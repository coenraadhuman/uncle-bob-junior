I'll write a reusable retry helper with a functional interface for the operation, max attempts, and fixed delay between retries.

Assumptions:
- Uses a generic functional interface so it works with operations that return values or throw exceptions
- Retries on any exception; throws the last exception if all attempts fail
- `InterruptedException` during sleep is allowed to propagate
- Delay is in milliseconds

```java
@FunctionalInterface
public interface Retryable<T> {
    T execute() throws Exception;
}
```

```java
public class RetryHelper {
    public static <T> T execute(Retryable<T> operation, int maxAttempts, long delayMs) 
            throws Exception {
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
}
```

**Usage example:**

```java
// Retry a method that returns a value
String result = RetryHelper.execute(
    () -> fetchDataFromApi(),
    3,      // maxAttempts
    1000    // delayMs between retries
);

// Retry a method that returns void (use Void type)
RetryHelper.execute(
    () -> { saveToDatabase(); return null; },
    5,
    500
);
```

The helper returns the result on success, retries on any exception, waits the specified delay between attempts, and throws the last exception if all attempts fail.