package com.prince.serverless.config;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AWS SQS client configuration
 *
 * @author Prince Raj
 */
@Component
@Data
public class AmazonSQSClientConfig {

    @Value("${com.example.aws.sqs.url}")
    private String queueUrl;

    @Value("${com.example.aws.sqs.region}")
    private String region;

    @Value("${com.example.aws.sqs.max.batch.size}")
    private int maxBatchSize;

    @Value("${com.example.aws.sqs.wait.time}")
    private int waitTime;

    @Value("${com.example.aws.sqs.visibility.timeout}")
    private int visibilityTimeout;
}
