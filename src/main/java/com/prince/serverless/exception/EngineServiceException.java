package com.prince.serverless.exception;

import com.prince.serverless.util.ExceptionErrorCode;

/**
 * This class provide callers several pieces of information that can be used to obtain more
 * information about the error and why it occurred.
 *
 * @author Prince Raj
 */
@SuppressWarnings({"unused"})
public class EngineServiceException extends EngineClientException {

    private static final long serialVersionUID = 1L;

    // unique request id
    private String requestId;

    public EngineServiceException(String message) {
        super(message);
    }

    public EngineServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public EngineServiceException(Throwable cause) {
        super(cause);
    }

    {
        this.setError(ExceptionErrorCode.INTERNAL_ERROR_OCCURRED);
    }
}
