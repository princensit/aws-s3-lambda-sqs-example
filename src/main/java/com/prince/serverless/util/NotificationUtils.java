package com.prince.serverless.util;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.NullLogChute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prince.serverless.accessor.AmazonSQSClientAccessor;
import com.prince.serverless.model.Communication;
import com.prince.serverless.model.FileItem;

/**
 * Utility for notification client
 *
 * @author Prince Raj
 */
@Component
public class NotificationUtils {

    private final AmazonSQSClientAccessor sqsClientAccessor;

    @Autowired
    public NotificationUtils(AmazonSQSClientAccessor sqsClientAccessor) {
        this.sqsClientAccessor = sqsClientAccessor;
    }

    public NotificationStatus sendEmail(FileItem fileItem) {
        String emailId = fileItem.getEmailId();

        final NotificationStatus status;
        if (fileItem.isSendEmail() && StringUtils.isNotEmpty(emailId)) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put(Constants.DATE, fileItem.getDate());
            dataMap.put(Constants.EMAIL_ID, emailId);

            String messageBody = JsonUtils.toJson(dataMap);

            boolean success = sqsClientAccessor.sendMessage(messageBody, Constants.EMAIL_GROUP_ID);
            if (success) {
                status = NotificationStatus.SUCCESS;
            } else {
                status = NotificationStatus.FAILURE;
            }
        } else {
            status = NotificationStatus.NOT_APPLICABLE;
        }

        return status;
    }

    public NotificationStatus sendSms(FileItem fileItem) {
        String mobileNumber = fileItem.getMobileNumber();

        final NotificationStatus status;
        if (fileItem.isSendSms() && StringUtils.isNotEmpty(mobileNumber)) {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put(Constants.DATE, fileItem.getDate());
            dataMap.put(Constants.MOBILE, mobileNumber);

            String messageBody = JsonUtils.toJson(dataMap);

            boolean success = sqsClientAccessor.sendMessage(messageBody, Constants.SMS_GROUP_ID);
            if (success) {
                status = NotificationStatus.SUCCESS;
            } else {
                status = NotificationStatus.FAILURE;
            }
        } else {
            status = NotificationStatus.NOT_APPLICABLE;
        }

        return status;
    }

    public void sendEmailToAdmin(Communication communication) {
        Map<String, String> dataMap = new HashMap<>();

        dataMap.put(Constants.TEMPLATE_ID, String.valueOf(communication.getTemplateId()));
        dataMap.put(Constants.EMAIL_ID, communication.getEmailId());
        dataMap.put(Constants.SUBJECT, getSubject(communication));
        dataMap.put(Constants.BODY, getBody(communication));

        String messageBody = JsonUtils.toJson(dataMap);

        sqsClientAccessor.sendMessage(messageBody, Constants.ADMIN_GROUP_ID);
    }

    private String getSubject(Communication communication) {
        String template = communication.getSubject();
        return populatePlaceholders(template, communication.getDataMap());
    }

    private String getBody(Communication communication) {
        String template = communication.getBody();
        return populatePlaceholders(template, communication.getDataMap());
    }

    private String populatePlaceholders(String template, final Map<String, String> dataMap) {
        // disable velocity.log
        Velocity.setProperty("runtime.log.logsystem.class", NullLogChute.class.getName());
        Velocity.init();

        final VelocityContext velocityContext = new VelocityContext();
        for (Map.Entry<String, String> entryMap : dataMap.entrySet()) {
            velocityContext.put(entryMap.getKey(), entryMap.getValue());
        }

        StringWriter writer = new StringWriter();
        Velocity.evaluate(velocityContext, writer, Constants.TEMPLATE_LOG_TAG, template);

        return writer.toString();
    }
}
