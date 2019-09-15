package com.prince.serverless.exception;

import com.prince.serverless.util.ExceptionErrorCode;

/**
 * This exception is thrown when invalid file is going to get processed
 *
 * @author Prince Raj
 */
@SuppressWarnings("unused")
public class InvalidFileException extends EngineClientException {

    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFileException(Throwable cause) {
        super(cause);
    }

    {
        this.setError(ExceptionErrorCode.S3_OBJECT_NOT_FOUND);
    }
}
