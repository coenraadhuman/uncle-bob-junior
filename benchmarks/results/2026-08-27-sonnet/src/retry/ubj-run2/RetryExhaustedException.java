// File: RetryExhaustedException.java
package com.example.retry;

public final class RetryExhaustedException extends Exception {

    public RetryExhaustedException(int attemptsMade, Exception lastFailure) {
        super("Operation failed after " + attemptsMade + " attempt(s)", lastFailure);
    }
}
