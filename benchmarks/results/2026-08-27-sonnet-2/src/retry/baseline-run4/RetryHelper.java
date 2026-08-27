import java.time.Duration;
import java.util.concurrent.Callable;

public final class RetryHelper {

    private RetryHelper() {
    }

    public static <T> T retry(Callable<T> operation, int maxAttempts, Duration delay) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative");
        }

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;

                if (attempt == maxAttempts) {
                    break;
                }

                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RetryExhaustedException(attempt, "Retry interrupted while waiting", ie);
                }
            }
        }

        throw new RetryExhaustedException(maxAttempts, "Operation failed after " + maxAttempts + " attempts", lastException);
    }

    public static void retry(RunnableWithException operation, int maxAttempts, Duration delay) {
        retry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delay);
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }

    public static final class RetryExhaustedException extends RuntimeException {
        private final int attemptsMade;

        public RetryExhaustedException(int attemptsMade, String message, Throwable cause) {
            super(message, cause);
            this.attemptsMade = attemptsMade;
        }

        public int getAttemptsMade() {
            return attemptsMade;
        }
    }
}
