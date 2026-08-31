Assumptions: the operation is expressed as `java.util.concurrent.Callable<T>` (so it can throw checked exceptions), retries happen on any `Exception`, delay is specified in milliseconds via `Thread.sleep`, and if all attempts fail the last exception is rethrown wrapped in a `RuntimeException` (unless it's already a `RuntimeException`).

```java
import java.util.concurrent.Callable;

/**
 * Reusable retry helper.
 */
public final class RetryHelper {

    private RetryHelper() {
    }

    /**
     * Runs the given operation, retrying on exception up to maxAttempts times,
     * waiting a fixed delay between attempts.
     *
     * @param operation   the operation to run
     * @param maxAttempts total number of attempts (must be >= 1)
     * @param delayMillis fixed delay between attempts, in milliseconds
     * @param <T>         the operation's return type
     * @return the result of the operation, if it eventually succeeds
     * @throws RuntimeException if all attempts fail, wrapping the last exception
     */
    public static <T> T run(Callable<T> operation, int maxAttempts, long delayMillis) {
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
                    Thread.sleep(delayMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted while waiting between attempts", ie);
                }
            }
        }

        if (lastException instanceof RuntimeException) {
            throw (RuntimeException) lastException;
        }
        throw new RuntimeException("Operation failed after " + maxAttempts + " attempts", lastException);
    }

    // Example usage
    public static void main(String[] args) {
        int[] callCount = {0};

        String result = RetryHelper.run(() -> {
            callCount[0]++;
            if (callCount[0] < 3) {
                throw new IllegalStateException("Simulated failure on attempt " + callCount[0]);
            }
            return "success on attempt " + callCount[0];
        }, 5, 200);

        System.out.println(result);
    }
}
```

Usage: call `RetryHelper.run(() -> yourOperation(), maxAttempts, delayMillis)`. It returns the result on success, or throws once all attempts are exhausted.