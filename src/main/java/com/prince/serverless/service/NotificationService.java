package com.prince.serverless.service;

import com.prince.serverless.model.EventResponse;

/**
 * @author Prince Raj
 */
public interface NotificationService {

    /**
     * Take input s3 file, parse the content and accordingly send notifications to users. Once the
     * processing is complete, the statistics are sent to admin users.
     *
     * @param objectKey s3 object key
     * @return event response
     * @throws Exception exception
     */
    EventResponse processFile(String objectKey) throws Exception;
}
