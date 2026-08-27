package com.postcodeloterij.retry;

/** Thrown when an operation still fails after all retry attempts are used up. */
public final class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int attemptsMade, Throwable lastFailure) {
        super("Operation failed after " + attemptsMade + " attempt(s)", lastFailure);
    }
}
