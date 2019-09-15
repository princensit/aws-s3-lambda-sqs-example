package com.prince.serverless.exception;

import com.prince.serverless.util.ExceptionErrorCode;

/**
 * This exception is thrown when object is not found in S3
 *
 * @author Prince Raj
 */
@SuppressWarnings("unused")
public class S3ObjectNotFoundException extends EngineServiceException {

    public S3ObjectNotFoundException(String message) {
        super(message);
    }

    public S3ObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public S3ObjectNotFoundException(Throwable cause) {
        super(cause);
    }

    {
        this.setError(ExceptionErrorCode.S3_OBJECT_NOT_FOUND);
    }
}
