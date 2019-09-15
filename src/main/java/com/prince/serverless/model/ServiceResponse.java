package com.prince.serverless.model;

import java.io.Serializable;

import lombok.Data;

/**
 * Wrapper response to be sent to client containing data, error and server timestamp
 *
 * @author Prince Raj
 */
@Data
public final class ServiceResponse<T> implements Serializable {

    // data to return in response
    private T data;

    // for any exception, error object will be populated
    private Error error;

    // timestamp when server returned the response
    private long timestamp;
}
