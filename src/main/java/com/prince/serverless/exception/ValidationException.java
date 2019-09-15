package com.prince.serverless.exception;

import com.prince.serverless.util.ExceptionErrorCode;

/**
 * This exception is thrown when request parameters are invalid
 *
 * @author Prince Raj
 */
@SuppressWarnings("unused")
public class ValidationException extends EngineClientException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    {
        this.setError(ExceptionErrorCode.INVALID_REQUEST_PARAMS);
    }
}
