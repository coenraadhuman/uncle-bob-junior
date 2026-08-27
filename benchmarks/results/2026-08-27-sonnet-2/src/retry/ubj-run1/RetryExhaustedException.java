public final class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int attemptsMade, Throwable lastFailure) {
        super("Operation failed after " + attemptsMade + " attempts", lastFailure);
    }
}
