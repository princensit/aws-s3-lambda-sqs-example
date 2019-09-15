package com.prince.serverless.exception;

import lombok.Getter;
import lombok.Setter;

import com.prince.serverless.util.ExceptionErrorCode;

/**
 * It indicates that a problem occurred inside the Java client code, either while trying to send a
 * request to server or while trying to parse a response from server.
 *
 * It is generally more severe than {@link EngineServiceException}, in a way that client is
 * prevented for making call to server. For example this exception is thrown when there is no
 * network connection available.
 *
 * Callers should typically deal with exceptions through {@link EngineServiceException}, which
 * represent error responses returned by services. {@link EngineServiceException} has much more
 * information available for callers to appropriately deal with different types of errors that can
 * occur.
 *
 * @see EngineServiceException
 * @author Prince Raj
 */
@SuppressWarnings({"unused"})
public class EngineClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private ExceptionErrorCode error;

    public EngineClientException(String message) {
        super(message);
    }

    public EngineClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public EngineClientException(Throwable cause) {
        super(cause);
    }

    /**
     * Default is true but subclass may override.
     *
     * @return flag if this exception is retryable or not
     */
    public boolean isRetryable() {
        return true;
    }
}
