// RetryInterruptedException.java
package retry;

/** Thrown when the thread is interrupted while waiting between retry attempts. */
public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Retry wait was interrupted", cause);
    }
}
