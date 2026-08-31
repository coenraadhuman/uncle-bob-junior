I'll create a reusable retry helper with support for operations that return values or are void, configurable attempts and delay, and proper exception handling.

**Assumptions:**
- Use functional interfaces for operations (checked exceptions)
- Retry on any exception (configurable if needed)
- Throw the last exception if all retries fail
- Fixed delay between attempts (no backoff)
- Support both void and return-value operations

```java
public class RetryHelper {

  public interface RetryableOperation<T> {
    T execute() throws Exception;
  }

  public interface RetryableAction {
    void execute() throws Exception;
  }

  private static final int DEFAULT_MAX_ATTEMPTS = 3;
  private static final long DEFAULT_DELAY_MS = 1000;

  public static <T> T retry(RetryableOperation<T> operation, int maxAttempts, long delayMs)
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

  public static void retry(RetryableAction action, int maxAttempts, long delayMs)
      throws Exception {
    retry(() -> {
      action.execute();
      return null;
    }, maxAttempts, delayMs);
  }

  public static <T> T retry(RetryableOperation<T> operation) throws Exception {
    return retry(operation, DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS);
  }

  public static void retry(RetryableAction action) throws Exception {
    retry(action, DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS);
  }
}
```

**Usage examples:**

```java
// Operation with return value
String result = RetryHelper.retry(
    () -> apiClient.fetchData(),
    3,
    1000
);

// Void operation
RetryHelper.retry(
    () -> database.save(record),
    5,
    2000
);

// Using defaults (3 attempts, 1s delay)
String data = RetryHelper.retry(() -> apiClient.fetchData());
```

The helper captures the last exception and rethrows it if all attempts fail, sleeps between attempts, and supports both value-returning and void operations through overloaded methods.