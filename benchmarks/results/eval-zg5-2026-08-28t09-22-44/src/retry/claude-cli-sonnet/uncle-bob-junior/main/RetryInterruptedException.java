package com.example.retry;

/** Thrown when the wait between retry attempts is interrupted. */
public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Retry wait was interrupted", cause);
    }
}
