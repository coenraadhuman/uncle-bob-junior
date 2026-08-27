// RetryExhaustedException.java
public final class RetryExhaustedException extends Exception {

    public RetryExhaustedException(int attempts, Throwable lastFailure) {
        super("Operation failed after " + attempts + " attempts", lastFailure);
    }
}
