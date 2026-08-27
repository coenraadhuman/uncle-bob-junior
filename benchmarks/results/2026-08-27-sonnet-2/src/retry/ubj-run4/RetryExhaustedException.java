public final class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int attempts, Throwable lastFailure) {
        super("Operation failed after " + attempts + " attempts", lastFailure);
    }
}
