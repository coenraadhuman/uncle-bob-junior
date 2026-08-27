package com.plg.retry;

/**
 * Thrown when a {@link RetryableTask} still fails after the configured
 * number of attempts. The last failure is available via {@link #getCause()}.
 */
public class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int attempts, Throwable lastFailure) {
        super("Operation failed after " + attempts + " attempt(s)", lastFailure);
    }
}
