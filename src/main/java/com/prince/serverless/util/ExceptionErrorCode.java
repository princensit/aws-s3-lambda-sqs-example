package com.prince.serverless.util;

import lombok.Getter;

/**
 * Exception error code mapping with message
 *
 * @author Prince Raj
 */
public enum ExceptionErrorCode {

    INTERNAL_ERROR_OCCURRED("ER-1001", "Internal error occurred. Please try again sometime."),

    INVALID_REQUEST_PARAMS("ER-1002", "Invalid request parameters"),

    S3_OBJECT_NOT_FOUND("ER-1005", "S3 object doesn't exists");

    @Getter
    private final String code;

    @Getter
    private final String message;

    ExceptionErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
