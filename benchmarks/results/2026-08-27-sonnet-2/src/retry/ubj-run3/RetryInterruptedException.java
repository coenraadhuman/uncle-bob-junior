// File: RetryInterruptedException.java
package com.plg.retry;

/**
 * Thrown when the current thread is interrupted while waiting
 * between retry attempts. The thread's interrupt status is restored
 * before this is thrown.
 */
public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Retry delay was interrupted", cause);
    }
}
