// RetryExhaustedException.java
package com.plg.retry;

public final class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int attemptsMade, Throwable lastFailure) {
        super("Operation failed after " + attemptsMade + " attempts", lastFailure);
    }
}
