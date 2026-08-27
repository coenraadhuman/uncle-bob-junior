// File: RetryExhaustedException.java
package com.plg.retry;

/**
 * Thrown when a {@link Retryer} runs out of attempts without the
 * operation succeeding. The cause is the exception from the last attempt.
 */
public final class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int maxAttempts, Throwable lastFailure) {
        super("Operation failed after " + maxAttempts + " attempts", lastFailure);
    }
}
