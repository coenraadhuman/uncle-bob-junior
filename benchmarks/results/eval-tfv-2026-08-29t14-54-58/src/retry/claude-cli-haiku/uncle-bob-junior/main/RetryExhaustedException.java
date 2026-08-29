public class RetryExhaustedException extends RuntimeException {
    public RetryExhaustedException(Throwable cause) {
        super("Retry exhausted after all attempts failed", cause);
    }
}
