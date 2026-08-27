// RetryExhaustedException.java
/**
 * Thrown when an operation still fails after all configured attempts.
 * The cause is the exception from the last attempt.
 */
public class RetryExhaustedException extends Exception {

    private final int attemptsMade;

    public RetryExhaustedException(int attemptsMade, Throwable cause) {
        super("Operation failed after " + attemptsMade + " attempt(s)", cause);
        this.attemptsMade = attemptsMade;
    }

    public int attemptsMade() {
        return attemptsMade;
    }
}
