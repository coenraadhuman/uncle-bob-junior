package com.example.retry;

/**
 * Thrown when an operation still fails after all retry attempts are used up.
 * The original failure from the last attempt is available via {@link #getCause()}.
 */
public class RetryExhaustedException extends RuntimeException {

    private final int attemptsMade;

    public RetryExhaustedException(int attemptsMade, Throwable lastFailure) {
        super("Operation failed after " + attemptsMade + " attempt(s)", lastFailure);
        this.attemptsMade = attemptsMade;
    }

    public int attemptsMade() {
        return attemptsMade;
    }
}
