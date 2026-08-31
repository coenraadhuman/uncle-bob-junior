```java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMs;

    public RetryHelper(int maxAttempts, long delayMs) {
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }

    public <T> T execute(Operation<T> operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        throw lastException;
    }

    public void executeVoid(VoidOperation operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        throw lastException;
    }

    @FunctionalInterface
    public interface Operation<T> {
        T run() throws Exception;
    }

    @FunctionalInterface
    public interface VoidOperation {
        void run() throws Exception;
    }
}
```

**Example usage:**

```java
// With return value
RetryHelper retry = new RetryHelper(3, 1000);

String result = retry.execute(() -> {
    return fetchDataFromAPI();
});

// Without return value
retry.executeVoid(() -> {
    sendRequest();
});
```

**Assumptions:**
- Max attempts and delay are configured once per helper instance
- Exceptions are propagated after all retries are exhausted
- The delay occurs between attempts (not after the final failed attempt)
- `Thread.sleep()` is acceptable (for non-blocking scenarios, you'd use async/await or scheduler patterns)