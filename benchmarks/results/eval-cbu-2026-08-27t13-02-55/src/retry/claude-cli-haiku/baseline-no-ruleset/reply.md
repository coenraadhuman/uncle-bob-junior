I'll provide a clean, reusable retry helper with reasonable assumptions built in.

**Assumptions:**
- Retries on any `Exception` (easily customizable for specific exception types)
- Uses `Callable<T>` to support operations that return values
- Thread-safe and handles `InterruptedException` from sleep
- Throws the last exception if all attempts are exhausted

Here's the solution:

```java
public class RetryHelper {
  private final int maxAttempts;
  private final long delayMillis;

  public RetryHelper(int maxAttempts, long delayMillis) {
    if (maxAttempts < 1) {
      throw new IllegalArgumentException("maxAttempts must be at least 1");
    }
    if (delayMillis < 0) {
      throw new IllegalArgumentException("delayMillis cannot be negative");
    }
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
}
```

**Usage examples:**

```java
// Basic usage
RetryHelper retry = new RetryHelper(3, 1000); // 3 attempts, 1 second delay

// Retry an operation that returns a value
String result = retry.execute(() -> {
  return callSomeService();
});

// Retry an operation without return value
retry.execute(() -> {
  updateDatabase();
  return null;
});

// Retry a lambda with checked exceptions
int value = retry.execute(() -> Integer.parseInt("42"));
```

**Enhanced version with builder pattern** (if you need more flexibility):

```java
public class RetryHelper {
  private final int maxAttempts;
  private final long delayMillis;
  private final Class<? extends Exception> retryOn;

  private RetryHelper(int maxAttempts, long delayMillis, 
                      Class<? extends Exception> retryOn) {
    this.maxAttempts = maxAttempts;
    this.delayMillis = delayMillis;
    this.retryOn = retryOn;
  }

  public static Builder builder() {
    return new Builder();
  }

  public <T> T execute(Callable<T> operation) throws Exception {
    Exception lastException = null;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        return operation.call();
      } catch (Exception e) {
        if (!retryOn.isInstance(e)) {
          throw e;
        }
        lastException = e;
        if (attempt < maxAttempts) {
          Thread.sleep(delayMillis);
        }
      }
    }

    throw lastException;
  }

  public static class Builder {
    private int maxAttempts = 3;
    private long delayMillis = 1000;
    private Class<? extends Exception> retryOn = Exception.class;

    public Builder maxAttempts(int maxAttempts) {
      this.maxAttempts = maxAttempts;
      return this;
    }

    public Builder delayMillis(long delayMillis) {
      this.delayMillis = delayMillis;
      return this;
    }

    public Builder retryOn(Class<? extends Exception> exceptionType) {
      this.retryOn = exceptionType;
      return this;
    }

    public RetryHelper build() {
      return new RetryHelper(maxAttempts, delayMillis, retryOn);
    }
  }
}
```

**Builder usage:**

```java
RetryHelper retry = RetryHelper.builder()
  .maxAttempts(5)
  .delayMillis(500)
  .retryOn(IOException.class)  // Only retry on specific exception
  .build();

String result = retry.execute(() -> fetchFromAPI());
```

Choose the simple version for straightforward needs, or the builder version for more control over exception types.