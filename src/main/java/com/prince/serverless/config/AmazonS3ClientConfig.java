package com.prince.serverless.config;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AWS S3 client configuration
 *
 * @author Prince Raj
 */
@Component
@Data
public class AmazonS3ClientConfig {

    @Value("${com.example.aws.s3.bucket.name}")
    private String bucketName;

    @Value("${com.example.aws.s3.region}")
    private String region;
}
