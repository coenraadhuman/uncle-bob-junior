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

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new RetryExhaustedException(
                            "Retry interrupted while waiting between attempts", interrupted);
                }
            }
        }

        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempt(s)", lastException);
    }

    public static final class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
