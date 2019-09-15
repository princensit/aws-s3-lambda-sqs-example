package com.prince.serverless.model;

import java.io.Serializable;

import lombok.Data;

/**
 * Error class exposed to client
 *
 * @author Prince Raj
 */
@Data
public class Error implements Serializable {

    // error code
    private String code;

    // error message
    private String message;
}
