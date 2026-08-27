package com.example.retry;

/**
 * Thrown when an operation still fails after all retry attempts are exhausted.
 * The cause is the exception from the last failed attempt.
 */
public final class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int attempts, Throwable lastFailure) {
        super("Operation failed after " + attempts + " attempts", lastFailure);
    }
}
