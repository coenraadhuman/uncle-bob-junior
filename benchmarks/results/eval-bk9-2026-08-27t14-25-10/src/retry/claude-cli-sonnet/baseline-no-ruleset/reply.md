Here's a reusable retry helper with generics so it works for any operation that returns a value (or void via a wrapper). Assumptions: fixed delay via `Thread.sleep`, retries only on `Exception` (configurable to a specific type if needed), and it rethrows the last exception wrapped once attempts are exhausted. Uses `TimeUnit` for the delay unit.

```java
import java.util.concurrent.TimeUnit;

/**
 * Generic retry helper: runs an operation, retrying on exception up to a
 * maximum number of attempts, waiting a fixed delay between attempts.
 */
public final class RetryHelper {

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T call() throws Exception;
    }

    public static class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private RetryHelper() {
    }

    /**
     * Executes the given operation, retrying on failure.
     *
     * @param operation   the operation to run
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delay       fixed wait time between attempts
     * @param delayUnit   unit for the delay
     * @return the operation's result on success
     * @throws RetryExhaustedException if all attempts fail
     */
    public static <T> T execute(RetryableOperation<T> operation,
                                 int maxAttempts,
                                 long delay,
                                 TimeUnit delayUnit) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
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
                    delayUnit.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RetryExhaustedException(
                            "Retry interrupted while waiting between attempts", ie);
                }
            }
        }

        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempts", lastException);
    }

    /**
     * Convenience overload for operations that don't return a value.
     */
    public static void execute(Runnable operation, int maxAttempts, long delay, TimeUnit delayUnit) {
        execute(() -> {
            operation.run();
            return null;
        }, maxAttempts, delay, delayUnit);
    }

    // Example usage
    public static void main(String[] args) {
        int[] attemptCounter = {0};

        String result = RetryHelper.execute(() -> {
            attemptCounter[0]++;
            if (attemptCounter[0] < 3) {
                throw new RuntimeException("Simulated failure on attempt " + attemptCounter[0]);
            }
            return "Success on attempt " + attemptCounter[0];
        }, 5, 500, TimeUnit.MILLISECONDS);

        System.out.println(result);
    }
}
```

Key design choices:
- `RetryableOperation<T>` allows checked exceptions and a return value; a `Runnable` overload covers void operations.
- Catches `Exception` (not `Throwable`) so `Error`s (e.g. `OutOfMemoryError`) propagate immediately rather than being retried.
- On exhaustion it throws `RetryExhaustedException` wrapping the last failure, rather than swallowing errors silently.
- Interruption during the sleep restores the interrupt flag and aborts immediately instead of continuing to retry.