// RetryInterruptedException.java
package retry;

public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(int attemptsCompleted, InterruptedException cause) {
        super("Retry loop interrupted after attempt " + attemptsCompleted, cause);
    }
}
