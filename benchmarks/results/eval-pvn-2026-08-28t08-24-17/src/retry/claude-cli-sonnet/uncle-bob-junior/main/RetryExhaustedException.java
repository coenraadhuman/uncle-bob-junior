package retry;

public class RetryExhaustedException extends RuntimeException {

    private final int attemptsMade;

    public RetryExhaustedException(int attemptsMade, Exception lastFailure) {
        super("Operation failed after " + attemptsMade + " attempts", lastFailure);
        this.attemptsMade = attemptsMade;
    }

    public int attemptsMade() {
        return attemptsMade;
    }
}
