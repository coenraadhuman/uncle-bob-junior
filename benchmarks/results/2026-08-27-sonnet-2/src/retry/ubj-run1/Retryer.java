import java.time.Duration;
import java.util.concurrent.Callable;

public final class Retryer {

    private Retryer() {
    }

    public static <T> T retry(Callable<T> operation, int maxAttempts, Duration delayBetweenAttempts) {
        requireValidAttemptCount(maxAttempts);

        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
            }
            if (attempt < maxAttempts) {
                sleep(delayBetweenAttempts);
            }
        }

        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private static void requireValidAttemptCount(int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
    }

    private static void sleep(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(interrupted);
        }
    }
}
