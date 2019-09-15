package com.prince.serverless.accessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

import com.prince.serverless.config.AmazonS3ClientConfig;
import com.prince.serverless.exception.S3ObjectNotFoundException;
import com.prince.serverless.util.FileUtils;

/**
 * S3 client accessor to perform CRUD operations on S3 bucket
 *
 * @author Prince Raj
 */
@Component
@SuppressWarnings("unused")
public class AmazonS3ClientAccessor {

    private final Logger log = LogManager.getLogger(this.getClass());

    private final AmazonS3ClientConfig s3ClientConfig;

    private AmazonS3 s3Client;

    private TransferManager transferManager;

    @Autowired
    public AmazonS3ClientAccessor(AmazonS3ClientConfig s3ClientConfig) {
        this.s3ClientConfig = s3ClientConfig;
    }

    @PostConstruct
    public void init() {
        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(s3ClientConfig.getRegion())).build();

        transferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build();
    }

    public BufferedReader readFromS3(String objectKey) throws IOException {
        if (doesObjectExists(objectKey)) {
            S3Object s3Object = s3Client.getObject(s3ClientConfig.getBucketName(), objectKey);
            return FileUtils.getBufferedReader(s3Object.getObjectContent(), objectKey);
        } else {
            throw new S3ObjectNotFoundException("File doesn't exists");
        }
    }

    public void uploadToS3(String inputFilePath, String s3FileKeyName) {
        try {
            File inputFile = new File(inputFilePath);
            Upload upload = transferManager.upload(s3ClientConfig.getBucketName(), s3FileKeyName,
                    inputFile);
            upload.waitForCompletion();
        } catch (AmazonClientException | InterruptedException ex) {
            log.error("Exception in uploading file to AWS S3 bucket: {}", ex.getMessage());
        }
    }

    public void deleteFromS3(String objectKey) {
        if (doesObjectExists(objectKey)) {
            s3Client.deleteObject(s3ClientConfig.getBucketName(), objectKey);
        }
    }

    private boolean doesObjectExists(String objectKey) {
        boolean exists;
        try {
            s3Client.getObjectMetadata(s3ClientConfig.getBucketName(), objectKey);
            exists = true;
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                exists = false;
            } else {
                throw e;
            }
        }

        return exists;
    }
}
