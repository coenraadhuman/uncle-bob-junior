package com.example.retry;

/** Thrown when an operation still fails after all retry attempts, or retrying is interrupted. */
public class RetryException extends RuntimeException {

    RetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
