Assumptions: the operation returns a value and may throw a checked exception; delay is fixed (not exponential backoff); the helper blocks the calling thread using `Thread.sleep`; after all attempts are exhausted it throws a `RetryException` wrapping the last failure.

```java
import java.time.Duration;
import java.util.Objects;

public final class RetryHelper {

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }

    public static final class RetryException extends RuntimeException {
        public RetryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private RetryHelper() {
    }

    public static <T> T runWithRetry(RetryableOperation<T> operation, int maxAttempts, Duration delay) {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(delay, "delay must not be null");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }

        Throwable lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RetryException("Retry interrupted on attempt " + attempt, e);
            } catch (Exception e) {
                lastFailure = e;
                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }
                sleep(delay);
            }
        }

        throw new RetryException("Operation failed after " + maxAttempts + " attempts", lastFailure);
    }

    private static void sleep(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RetryException("Retry interrupted while waiting between attempts", e);
        }
    }

    // Example usage
    public static void main(String[] args) {
        int[] callCount = {0};

        String result = runWithRetry(() -> {
            callCount[0]++;
            if (callCount[0] < 3) {
                throw new RuntimeException("Simulated failure on attempt " + callCount[0]);
            }
            return "success";
        }, 5, Duration.ofMillis(200));

        System.out.println("Result: " + result + " after " + callCount[0] + " attempts");
    }
}
```