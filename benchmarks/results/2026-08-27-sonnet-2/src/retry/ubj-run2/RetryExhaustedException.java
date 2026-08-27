package com.plg.retry;

/** Thrown when an operation still fails after the configured number of attempts. */
public final class RetryExhaustedException extends RuntimeException {

    private final int attemptsMade;

    public RetryExhaustedException(int attemptsMade, Throwable lastFailure) {
        super("Operation failed after " + attemptsMade + " attempt(s)", lastFailure);
        this.attemptsMade = attemptsMade;
    }

    public int attemptsMade() {
        return attemptsMade;
    }
}
