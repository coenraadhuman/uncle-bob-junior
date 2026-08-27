// RetryExhaustedException.java
package com.plg.retry;

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
