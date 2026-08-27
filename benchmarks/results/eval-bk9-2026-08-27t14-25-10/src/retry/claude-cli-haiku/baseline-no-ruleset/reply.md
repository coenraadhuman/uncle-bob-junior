I'll create a reusable retry helper with support for operations with return values and void operations, configurable attempts and delays.

```java
public class RetryHelper {
    
    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }
    
    @FunctionalInterface
    public interface VoidOperation {
        void execute() throws Exception;
    }
    
    /**
     * Executes an operation with retries on exception.
     * 
     * @param operation the operation to execute
     * @param maxAttempts maximum number of attempts (must be >= 1)
     * @param delayMs delay in milliseconds between attempts
     * @return the result of the operation
     * @throws Exception if all attempts fail
     */
    public static <T> T retry(Operation<T> operation, int maxAttempts, long delayMs) throws Exception {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        
        throw lastException;
    }
    
    /**
     * Executes a void operation with retries on exception.
     * 
     * @param operation the operation to execute
     * @param maxAttempts maximum number of attempts (must be >= 1)
     * @param delayMs delay in milliseconds between attempts
     * @throws Exception if all attempts fail
     */
    public static void retryVoid(VoidOperation operation, int maxAttempts, long delayMs) throws Exception {
        retry(() -> {
            operation.execute();
            return null;
        }, maxAttempts, delayMs);
    }
}
```

**Usage examples:**

```java
// Operation with return value
String result = RetryHelper.retry(
    () -> fetchDataFromApi(),
    3,           // max 3 attempts
    1000         // 1 second delay between attempts
);

// Void operation
RetryHelper.retryVoid(
    () -> saveToDatabase(),
    5,           // max 5 attempts
    500          // 500ms delay between attempts
);

// With inline operation
int value = RetryHelper.retry(
    () -> {
        // Your operation logic here
        return someExpensiveCall();
    },
    2,
    2000
);
```

**Assumptions:**
- Max attempts must be at least 1 (validated)
- Delays are in milliseconds
- All exceptions are retried (no filtering by exception type)
- The original exception is thrown if all attempts fail
- Delay only occurs between attempts, not after the final failure