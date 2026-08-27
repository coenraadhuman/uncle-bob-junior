// Retry.java
import java.time.Duration;

public final class Retry {

    private Retry() {
    }

    public static <T> T execute(RetryableOperation<T> operation, int maxAttempts, Duration delayBetweenAttempts)
            throws RetryExhaustedException {
        requireValidMaxAttempts(maxAttempts);

        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (Exception e) {
                lastFailure = e;
                if (attempt < maxAttempts) {
                    waitBeforeNextAttempt(delayBetweenAttempts);
                }
            }
        }
        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private static void requireValidMaxAttempts(int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, got " + maxAttempts);
        }
    }

    private static void waitBeforeNextAttempt(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(e);
        }
    }
}
