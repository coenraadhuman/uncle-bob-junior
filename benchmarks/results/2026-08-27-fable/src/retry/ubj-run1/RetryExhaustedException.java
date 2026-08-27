package retry;

/**
 * Thrown when an operation still fails after the configured number of attempts.
 * The last failure is the cause; earlier failures are attached as suppressed exceptions.
 */
public final class RetryExhaustedException extends Exception {

    RetryExhaustedException(int attempts, Exception lastFailure) {
        super("Operation failed after " + attempts + " attempts", lastFailure);
    }
}
