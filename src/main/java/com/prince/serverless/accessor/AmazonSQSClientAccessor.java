package com.prince.serverless.accessor;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import com.prince.serverless.config.AmazonSQSClientConfig;

/**
 * SQS client accessor to perform CRUD operations on SQS
 *
 * @author Prince Raj
 */
@Component
public class AmazonSQSClientAccessor {

    private final Logger log = LogManager.getLogger(this.getClass());

    private final AmazonSQSClientConfig sqsClientConfig;

    private AmazonSQS sqsClient;

    @Autowired
    public AmazonSQSClientAccessor(AmazonSQSClientConfig sqsClientConfig) {
        this.sqsClientConfig = sqsClientConfig;
    }

    @PostConstruct
    public void init() {
        sqsClient = AmazonSQSClientBuilder.standard()
                .withRegion(Regions.fromName(sqsClientConfig.getRegion())).build();
    }

    @Retryable(backoff = @Backoff(delay = 100, multiplier = 2))
    public boolean sendMessage(String messageBody, String groupId) {
        SendMessageRequest request =
                new SendMessageRequest().withQueueUrl(sqsClientConfig.getQueueUrl())
                        .withMessageBody(messageBody).withMessageGroupId(groupId);

        sqsClient.sendMessage(request);
        return true;
    }

    @Retryable(backoff = @Backoff(delay = 100, multiplier = 2))
    public boolean sendMessageBatch(List<String> messageBodies, String groupId) {
        List<SendMessageBatchRequestEntry> messageEntries = new ArrayList<>();

        for (String messageBody : messageBodies) {
            SendMessageBatchRequestEntry entry = new SendMessageBatchRequestEntry()
                    .withMessageBody(messageBody).withMessageGroupId(groupId);
            messageEntries.add(entry);
        }

        SendMessageBatchRequest request = new SendMessageBatchRequest()
                .withQueueUrl(sqsClientConfig.getQueueUrl()).withEntries(messageEntries);

        sqsClient.sendMessageBatch(request);
        return true;
    }

    public Message receiveMessage() {
        ReceiveMessageRequest request = new ReceiveMessageRequest()
                .withQueueUrl(sqsClientConfig.getQueueUrl()).withMaxNumberOfMessages(1)
                .withWaitTimeSeconds(sqsClientConfig.getWaitTime())
                .withVisibilityTimeout(sqsClientConfig.getVisibilityTimeout());

        List<Message> messages = sqsClient.receiveMessage(request).getMessages();

        Message message = null;
        if (!messages.isEmpty()) {
            message = messages.get(0);
        }

        return message;
    }

    public List<Message> receiveMessages() {
        ReceiveMessageRequest request =
                new ReceiveMessageRequest().withQueueUrl(sqsClientConfig.getQueueUrl())
                        .withMaxNumberOfMessages(sqsClientConfig.getMaxBatchSize())
                        .withWaitTimeSeconds(sqsClientConfig.getWaitTime())
                        .withVisibilityTimeout(sqsClientConfig.getVisibilityTimeout());

        return sqsClient.receiveMessage(request).getMessages();
    }

    public boolean deleteMessage(Message message) {
        DeleteMessageRequest request =
                new DeleteMessageRequest().withQueueUrl(sqsClientConfig.getQueueUrl())
                        .withReceiptHandle(message.getReceiptHandle());

        sqsClient.deleteMessage(request);
        return true;
    }

    public boolean deleteMessageBatch(List<Message> messages) {
        List<DeleteMessageBatchRequestEntry> messageEntries = new ArrayList<>();
        for (Message message : messages) {
            DeleteMessageBatchRequestEntry entry = new DeleteMessageBatchRequestEntry()
                    .withReceiptHandle(message.getReceiptHandle());
            messageEntries.add(entry);
        }

        DeleteMessageBatchRequest request = new DeleteMessageBatchRequest()
                .withQueueUrl(sqsClientConfig.getQueueUrl()).withEntries(messageEntries);

        sqsClient.deleteMessageBatch(request);
        return true;
    }

    @Recover
    public boolean recover(Exception ex) {
        log.error("Exception while sending message to queue: {}", ex.getMessage());

        return false;
    }
}
