package com.postcodeloterij.retry;

/** Thrown when the current thread is interrupted while waiting to retry. */
public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Interrupted while waiting to retry", cause);
    }
}
