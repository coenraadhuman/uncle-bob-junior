I'll create a flexible retry helper using functional interfaces. Here's a complete solution:

```java
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class RetryHelper {
    private final int maxAttempts;
    private final long delayMs;

    public RetryHelper(int maxAttempts, long delayMs) {
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }

    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
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
                operation.execute();
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
    public interface VoidOperation {
        void execute() throws Exception;
    }
}
```

**Usage examples:**

```java
// For operations that return a value
RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1s delay

String result = retry.execute(() -> {
    return fetchDataFromApi();
});

// For void operations
retry.executeVoid(() -> {
    saveDataToDatabase(result);
});

// With custom exceptions
try {
    Integer count = retry.execute(() -> {
        if (Math.random() < 0.7) {
            throw new IOException("Network error");
        }
        return 42;
    });
} catch (Exception e) {
    System.err.println("Failed after retries: " + e.getMessage());
}
```

**Assumptions made:**
- Max attempts includes the initial attempt (so 3 attempts = 1 initial + 2 retries)
- Fixed delay in milliseconds between attempts
- Delay only occurs between attempts, not after the final failure
- Preserves the last exception to throw if all attempts fail
- Supports both value-returning and void operations