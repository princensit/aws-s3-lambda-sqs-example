package com.prince.serverless.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.io.CharStreams;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import com.prince.serverless.accessor.AmazonS3ClientAccessor;
import com.prince.serverless.config.AmazonS3ClientConfig;
import com.prince.serverless.model.Communication;
import com.prince.serverless.model.EventResponse;
import com.prince.serverless.model.FileDetails;
import com.prince.serverless.model.FileItem;
import com.prince.serverless.model.Stats;
import com.prince.serverless.util.ChannelType;
import com.prince.serverless.util.Constants;
import com.prince.serverless.util.FileUtils;
import com.prince.serverless.util.NotificationStatus;
import com.prince.serverless.util.NotificationUtils;
import com.prince.serverless.util.ReportType;

/**
 * This class is responsible to send messages to users for active/inactive notification activity.
 * Type of files that are supported are "Inactive" and "Active".
 *
 * Once file is processed, statistics are sent to admin users.
 *
 * <pre>
 * Sample input files:
 * s3://bucket/input/20190101/InactiveUserFile_20190701.csv000
 * s3://bucket/input/20190101/ActiveUserFile_20190701.csv000
 *
 * Sample output files:
 * s3://bucket/output/20190101/InactiveUserFile_20190701_<millis>.output.csv000
 * s3://bucket/output/20190101/ActiveUserFile_20190701_<millis>.output.csv000
 * </pre>
 *
 * @author Prince Raj
 */
@Component
@SuppressWarnings({"unused"})
public class NotificationServiceImpl implements NotificationService {

    private final Logger log = LogManager.getLogger(this.getClass());

    private final AmazonS3ClientConfig s3ClientConfig;

    private final AmazonS3ClientAccessor s3ClientAccessor;

    private final NotificationUtils notificationUtils;

    @Value("${com.example.admin.enabled.email:false}")
    private boolean adminEmailEnabled;

    @Value("#{'${com.example.admin.emailids}'.split(',')}")
    private List<String> adminEmailIds;

    @Value("${com.example.admin.email.template}")
    private int emailTemplate;

    @Autowired
    public NotificationServiceImpl(AmazonS3ClientConfig s3ClientConfig,
            AmazonS3ClientAccessor s3ClientAccessor, NotificationUtils notificationUtils) {
        this.s3ClientConfig = s3ClientConfig;
        this.s3ClientAccessor = s3ClientAccessor;
        this.notificationUtils = notificationUtils;
    }

    @Override
    public EventResponse processFile(String objectKey) throws Exception {
        long startTime = System.currentTimeMillis();

        log.info("Starting Notification activity for s3 bucket: {} and object: {}",
                s3ClientConfig.getBucketName(), objectKey);

        FileDetails fileDetails = getFileDetails(objectKey);
        String fileName = fileDetails.getFileName();
        String lastFolderName = fileDetails.getLastFolderName();
        String inputFilePath = fileDetails.getFilePath();

        Map<ChannelType, Stats> channelTypeStatsMap = new HashMap<>();

        // validate file path
        if (inputFilePath.startsWith(Constants.BASE_INPUT_FOLDER_NAME)) {

            // check validity of file name
            ReportType reportType = ReportType.getReportType(fileName);

            // create output temp files
            String outputFileName = getOutputFileName(fileName);
            String tempOutputFilePath = createTempFile(outputFileName);

            try (CSVReader csvReader = getCSVReader(inputFilePath);
                    CSVWriter csvWriter = getCSVWriter(tempOutputFilePath)) {

                // validate headers
                String[] items = csvReader.readNext();
                boolean status = validateHeaders(items);
                if (status) {
                    // write output headers
                    csvWriter.writeNext(Constants.OUTPUT_FILE_HEADERS);

                    while ((items = csvReader.readNext()) != null) {
                        FileItem fileItem = getFileItem(items);

                        // send notifications to user
                        sendNotifications(csvWriter, fileItem, reportType, channelTypeStatsMap);
                    }
                } else {
                    log.error("Input file: {} is not in correct format", inputFilePath);
                }
            }

            // upload output file to S3
            if (!FileUtils.isEmpty(tempOutputFilePath)) {
                String outputFilePath = getOutputFilePath(outputFileName, lastFolderName);
                log.info("Uploading output file to s3 path: {}", outputFilePath);
                s3ClientAccessor.uploadToS3(tempOutputFilePath, outputFilePath);
            }
        } else {
            log.error("Input file: {} is not supported", inputFilePath);
        }

        long endTime = System.currentTimeMillis();
        long totalExecutionTime = (endTime - startTime) / 1000 / 60;

        // send email to admin with current stats
        sendEmailToAdmin(fileName, totalExecutionTime, channelTypeStatsMap);

        EventResponse response = new EventResponse();
        response.setFileName(fileName);
        response.setTotalExecutionTime(totalExecutionTime);
        response.setChannelTypeStatsMap(channelTypeStatsMap);

        log.info("Completed Notification activity for s3 bucket: {} and object: {}, with stats: {}",
                s3ClientConfig.getBucketName(), objectKey, response);

        return response;
    }

    private void sendNotifications(CSVWriter csvWriter, FileItem fileItem, ReportType reportType,
            Map<ChannelType, Stats> channelTypeStatsMap) {
        NotificationStatus emailStatus = notificationUtils.sendEmail(fileItem);
        updateStats(ChannelType.EMAIL, emailStatus, channelTypeStatsMap);

        NotificationStatus smsStatus = notificationUtils.sendSms(fileItem);
        updateStats(ChannelType.SMS, smsStatus, channelTypeStatsMap);

        String[] items = {fileItem.getUserId(), emailStatus.name(), smsStatus.name()};
        csvWriter.writeNext(items);
    }

    private void sendEmailToAdmin(String fileName, long totalExecutionTime,
            Map<ChannelType, Stats> channelTypeStatsMap) throws IOException {
        if (adminEmailEnabled) {
            Map<String, String> dataMap = new HashMap<>();
            populateDataMap(fileName, totalExecutionTime, dataMap, channelTypeStatsMap);
            String body = getNotificationActivityEmailBody();

            for (String emailId : adminEmailIds) {
                dataMap.put(Constants.NAME, emailId);

                Communication communication = new Communication();
                communication.setTemplateId(emailTemplate);
                communication.setEmailId(emailId);
                communication.setSubject(Constants.NOTIFICATION_ACTIVITY_SUBJECT_TEMPLATE);
                communication.setBody(body);
                communication.setDataMap(dataMap);

                notificationUtils.sendEmailToAdmin(communication);
            }
        }
    }

    private void populateDataMap(String fileName, long totalExecutionTime,
            Map<String, String> dataMap, Map<ChannelType, Stats> channelTypeStatsMap) {
        // date in IST timezone
        DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss zzz");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        String date = sdf.format(new Date());
        dataMap.put(Constants.DATE, date);

        dataMap.put(Constants.TOTAL_EXECUTION_TIME, String.valueOf(totalExecutionTime));
        dataMap.put(Constants.FILE_NAME, fileName);
        dataMap.put(Constants.FILE_TYPE, fileName.split(Constants.UNDERSCORE)[0]);

        for (ChannelType channelType : ChannelType.values()) {
            Stats stats = channelTypeStatsMap.get(channelType);
            String totalUsers = String.valueOf(stats.getTotalUsers());
            String successUsers = String.valueOf(stats.getSuccessUsers());
            String failedUsers = String.valueOf(stats.getFailedUsers());
            String notApplicableUsers = String.valueOf(stats.getNotApplicableUsers());

            switch (channelType) {
                case EMAIL:
                    dataMap.put(Constants.EMAIL_TOTAL_USERS_COUNT, totalUsers);
                    dataMap.put(Constants.EMAIL_SUCCESS_USERS_COUNT, successUsers);
                    dataMap.put(Constants.EMAIL_FAILED_USERS_COUNT, failedUsers);
                    dataMap.put(Constants.EMAIL_NOT_APPLICABLE_USERS_COUNT, notApplicableUsers);
                    break;
                case SMS:
                    dataMap.put(Constants.SMS_TOTAL_USERS_COUNT, totalUsers);
                    dataMap.put(Constants.SMS_SUCCESS_USERS_COUNT, successUsers);
                    dataMap.put(Constants.SMS_FAILED_USERS_COUNT, failedUsers);
                    dataMap.put(Constants.SMS_NOT_APPLICABLE_USERS_COUNT, notApplicableUsers);
                    break;
            }
        }
    }

    private String getNotificationActivityEmailBody() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream(Constants.NOTIFICATION_ACTIVITY_BODY_TEMPLATE);

        final String body;
        try (BufferedReader reader = FileUtils.getBufferedReader(inputStream)) {
            body = CharStreams.toString(reader);
        }

        return body;
    }

    private void updateStats(ChannelType channelType, NotificationStatus notificationStatus,
            Map<ChannelType, Stats> channelTypeStatsMap) {
        Stats stats = channelTypeStatsMap.get(channelType);
        if (stats == null) {
            stats = new Stats();
            channelTypeStatsMap.put(channelType, stats);
        }

        switch (notificationStatus) {
            case SUCCESS:
                stats.incrementSuccessUsersCount();
                break;
            case FAILURE:
                stats.incrementFailedUsersCount();
                break;
            case NOT_APPLICABLE:
                stats.incrementNotApplicableUsersCount();
                break;
        }
    }

    private FileDetails getFileDetails(String objectKey) throws UnsupportedEncodingException {
        // Object key may have spaces or unicode non-ASCII characters
        String filePath = objectKey.replace('+', ' ');
        filePath = URLDecoder.decode(filePath, Constants.UTF_8);

        Path path = Paths.get(filePath);

        return new FileDetails(path.getFileName().toString(),
                path.getParent().getFileName().toString(), filePath);
    }

    private String createTempFile(String fileName) throws IOException {
        String filePath = Constants.TEMP_FOLDER + fileName;
        FileUtils.createNewFile(filePath);

        return filePath;
    }

    private String getOutputFilePath(String fileName, String lastFolderName) {
        return Constants.BASE_OUTPUT_FOLDER_NAME + lastFolderName + Constants.FORWARD_SLASH
                + fileName;
    }

    private String getOutputFileName(String fileName) {
        return FilenameUtils.getBaseName(fileName) + Constants.UNDERSCORE
                + System.currentTimeMillis() + Constants.DOT + Constants.OUTPUT_FILE_SEPARATOR
                + Constants.DOT + FilenameUtils.getExtension(fileName);
    }

    private boolean validateHeaders(String[] items) {
        return Arrays.equals(Constants.INPUT_FILE_HEADERS, items);
    }

    private CSVReader getCSVReader(String filePath) throws IOException {
        return new CSVReader(s3ClientAccessor.readFromS3(filePath));
    }

    private CSVWriter getCSVWriter(String filePath) throws IOException {
        return new CSVWriter(FileUtils.getBufferedWriter(filePath, true),
                CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.NO_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
    }

    private FileItem getFileItem(String[] items) {
        int index = 0;

        FileItem fileItem = new FileItem();
        fileItem.setUserId(items[index++]);
        fileItem.setEmailId(items[index++]);
        fileItem.setMobileNumber(items[index++]);
        fileItem.setDate(items[index++]);
        fileItem.setSendEmail(parseBoolean(items[index++]));
        fileItem.setSendSms(parseBoolean(items[index]));

        return fileItem;
    }

    private boolean parseBoolean(String val) {
        return "1".equals(val);
    }
}
