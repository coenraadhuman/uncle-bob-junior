```java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryHelper(int maxAttempts, long delayMillis) {
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }

        throw lastException;
    }

    public void executeVoid(VoidCallable operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.call();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }

        throw lastException;
    }

    @FunctionalInterface
    public interface VoidCallable {
        void call() throws Exception;
    }
}
```

**Usage examples:**

```java
// Retry an operation that returns a value
RetryHelper retrier = new RetryHelper(3, 1000); // 3 attempts, 1s delay

String result = retrier.execute(() -> {
    // Your operation
    return fetchDataFromAPI();
});

// Retry an operation without return value
retrier.executeVoid(() -> {
    sendRequest();
});
```

**Assumptions:**
- Retries on any exception; catches and rethrows the last exception after all attempts fail
- Uses `Thread.sleep()` for delays (blocking; use virtual threads or `ScheduledExecutorService` for non-blocking in high-concurrency scenarios)
- No delay after the final failed attempt
- Attempts are counted 1 to maxAttempts (so maxAttempts=3 means 3 total attempts, not 3 retries)