I'll provide a reusable retry helper with support for operations that return values and operations with no return value.

**Assumptions:**
- Fixed delay between retries (no exponential backoff)
- Retries on any `Exception`
- Propagates the last exception after max attempts exhausted
- Uses standard Java `Callable` and `Runnable` interfaces

```java
import java.util.concurrent.Callable;

public class RetryHelper {
    
    public static <T> T executeWithRetry(Callable<T> operation, int maxAttempts, long delayMs) 
            throws Exception {
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
    
    public static void executeWithRetry(Runnable operation, int maxAttempts, long delayMs) 
            throws Exception {
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
}
```

**Usage examples:**

```java
// Operation that returns a value
String result = RetryHelper.executeWithRetry(
    () -> fetchDataFromAPI(),
    3,      // max attempts
    1000    // 1 second delay
);

// Operation with no return value
RetryHelper.executeWithRetry(
    () -> saveToDatabase(),
    5,      // max attempts
    2000    // 2 second delay
);
```