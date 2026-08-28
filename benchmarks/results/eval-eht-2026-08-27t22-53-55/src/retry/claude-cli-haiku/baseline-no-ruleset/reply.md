```java
import java.util.concurrent.Callable;

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
    
    public void execute(Runnable operation) throws Exception {
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
// For operations that return a value
RetryHelper retrier = new RetryHelper(3, 1000);

String result = retrier.execute(() -> {
    return someOperationThatMightFail();
});

// For operations with no return value
retrier.execute(() -> {
    someTaskThatMightFail();
});
```

**Assumptions:**
- Fixed delay between attempts (not exponential backoff)
- Retries on any `Exception` and throws the last one if all attempts fail
- Generic type support for operations returning values
- Uses `Thread.sleep()` for delays between attempts
- Does not retry successful operations