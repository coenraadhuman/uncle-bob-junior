I'll create a reusable retry helper that handles both operations with return values and void operations.

**Assumptions:**
- Maximum attempts includes the initial attempt
- Delay is in milliseconds between retries (not before the first attempt)
- Retries on any exception; the last exception is thrown if all attempts fail
- Supports generic return types via `Callable`

```java
public class RetryHelper {
    
    public static <T> T retry(Callable<T> operation, int maxAttempts, long delayMillis) 
            throws Exception {
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
    
    public static void retryVoid(RunnableThrows operation, int maxAttempts, long delayMillis) 
            throws Exception {
        retry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delayMillis);
    }
    
    @FunctionalInterface
    public interface RunnableThrows {
        void run() throws Exception;
    }
}
```

**Usage examples:**

```java
// Operation that returns a value
String result = RetryHelper.retry(
    () -> fetchDataFromAPI(),
    3,           // max attempts
    1000         // 1 second delay between retries
);

// Void operation
RetryHelper.retryVoid(
    () -> sendNotification(),
    5,           // max attempts
    2000         // 2 second delay between retries
);

// With lambda capturing variables
int userId = 123;
String data = RetryHelper.retry(
    () -> database.query("SELECT * FROM users WHERE id = " + userId),
    2,
    500
);
```

The helper throws the last caught exception if all attempts are exhausted, preserving the full exception stack trace for debugging.