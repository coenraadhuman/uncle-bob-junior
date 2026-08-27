// RetryExhaustedException.java
package retry;

public final class RetryExhaustedException extends RuntimeException {

    private final int attempts;

    public RetryExhaustedException(int attempts, Throwable lastFailure) {
        super("Operation did not succeed after " + attempts + " attempt(s)", lastFailure);
        this.attempts = attempts;
    }

    public int getAttempts() {
        return attempts;
    }
}
