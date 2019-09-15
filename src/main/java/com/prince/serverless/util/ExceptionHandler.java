package com.prince.serverless.util;

import com.prince.serverless.exception.EngineServiceException;
import com.prince.serverless.exception.InvalidFileException;
import com.prince.serverless.exception.S3ObjectNotFoundException;
import com.prince.serverless.exception.ValidationException;
import com.prince.serverless.model.Error;

/**
 * Custom exception handler to map exception to specific error object
 *
 * @author Prince Raj
 */
public class ExceptionHandler {

    public static Error handleException(Exception ex) {
        final ExceptionErrorCode exceptionErrorCode;
        if (ex instanceof ValidationException) {
            exceptionErrorCode = ((ValidationException) ex).getError();
        } else if (ex instanceof InvalidFileException) {
            exceptionErrorCode = ((InvalidFileException) ex).getError();
        } else if (ex instanceof S3ObjectNotFoundException) {
            exceptionErrorCode = ((S3ObjectNotFoundException) ex).getError();
        } else {
            exceptionErrorCode = new EngineServiceException("Something went wrong").getError();
        }

        Error error = new Error();
        error.setCode(exceptionErrorCode.getCode());
        error.setMessage(exceptionErrorCode.getMessage());

        return error;
    }
}
