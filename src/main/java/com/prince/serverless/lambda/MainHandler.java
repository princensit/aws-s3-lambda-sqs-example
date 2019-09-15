package com.prince.serverless.lambda;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;

import com.prince.serverless.config.SpringConfig;
import com.prince.serverless.model.Error;
import com.prince.serverless.model.EventResponse;
import com.prince.serverless.model.ServiceResponse;
import com.prince.serverless.service.NotificationService;
import com.prince.serverless.util.ExceptionHandler;

/**
 * Handler class that should extend AbstractHandler<T> where T should be a Spring @Configuration
 * class for Spring DI manager
 *
 * @author Prince Raj
 */
@SuppressWarnings("unused")
public class MainHandler extends AbstractHandler<SpringConfig>
        implements RequestHandler<S3Event, ServiceResponse<EventResponse>> {

    private final Logger log = LogManager.getLogger(this.getClass());

    /**
     * This main handler method gets invoked when Lambda function is invoked, as it implements
     * RequestHandler interface.
     *
     * This method name should be configured in the AWS Console. {@link MainHandler}
     *
     * As the best practice, this method should be kept very short and all the business logic should
     * sit in "Service" instance that we will fetch from Spring IoC container and will enjoy from
     * all Spring IoC features.
     *
     * @param s3Event S3 event object
     * @param context Context object
     * @return Event response
     * @see RequestHandler
     */
    public ServiceResponse<EventResponse> handleRequest(S3Event s3Event, Context context) {
        NotificationService businessService =
                getApplicationContext().getBean(NotificationService.class);

        EventResponse eventResponse = null;
        Error error = null;

        try {
            String objectKey = s3Event.getRecords().get(0).getS3().getObject().getKey();
            eventResponse = businessService.processFile(objectKey);
        } catch (Exception ex) {
            ex.printStackTrace();
            error = ExceptionHandler.handleException(ex);
        }

        ServiceResponse<EventResponse> response = new ServiceResponse<>();
        response.setData(eventResponse);
        response.setError(error);
        response.setTimestamp(System.currentTimeMillis());

        return response;
    }
}
