// RetryInterruptedException.java
package com.plg.retry;

public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Retry was interrupted while waiting between attempts", cause);
    }
}
