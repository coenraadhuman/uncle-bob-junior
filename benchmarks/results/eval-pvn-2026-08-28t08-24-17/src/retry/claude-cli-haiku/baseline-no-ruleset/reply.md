```java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMillis;

    public RetryHelper(int maxAttempts, long delayMillis) {
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public <T> T execute(Operation<T> operation) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
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
    public interface Operation<T> {
        T run() throws Exception;
    }
}
```

**Usage example:**

```java
RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1 second delay

// Retry a string operation
String result = retry.execute(() -> {
    // Your operation that might throw an exception
    return fetchDataFromApi();
});

// Retry an operation with no return value
retry.execute(() -> {
    saveToDatabase();
    return null;
});
```

**Assumptions:**
- Max attempts includes the first attempt (3 attempts = 1 initial + 2 retries)
- Delay occurs only between attempts, not before the first attempt
- Any exception type will trigger a retry
- The operation interface allows checked exceptions for flexibility