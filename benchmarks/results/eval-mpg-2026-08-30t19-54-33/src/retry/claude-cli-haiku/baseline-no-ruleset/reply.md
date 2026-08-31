Here's a reusable retry helper in Java with a builder pattern for flexibility:

```java
public class RetryHelper<T> {
    private final Callable<T> operation;
    private final int maxAttempts;
    private final long delayMillis;
    private final Class<? extends Exception>[] retryableExceptions;

    private RetryHelper(Builder<T> builder) {
        this.operation = builder.operation;
        this.maxAttempts = builder.maxAttempts;
        this.delayMillis = builder.delayMillis;
        this.retryableExceptions = builder.retryableExceptions;
    }

    public T execute() throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;

                if (attempt < maxAttempts && shouldRetry(e)) {
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                } else if (attempt == maxAttempts) {
                    break;
                }
            }
        }

        throw lastException;
    }

    private boolean shouldRetry(Exception e) {
        if (retryableExceptions.length == 0) {
            return true;
        }
        for (Class<? extends Exception> exceptionType : retryableExceptions) {
            if (exceptionType.isInstance(e)) {
                return true;
            }
        }
        return false;
    }

    public static <T> Builder<T> builder(Callable<T> operation) {
        return new Builder<>(operation);
    }

    public static Builder<Void> builder(Runnable operation) {
        return new Builder<>(() -> {
            operation.run();
            return null;
        });
    }

    public static class Builder<T> {
        private final Callable<T> operation;
        private int maxAttempts = 3;
        private long delayMillis = 1000;
        private Class<? extends Exception>[] retryableExceptions = new Class[0];

        private Builder(Callable<T> operation) {
            this.operation = operation;
        }

        public Builder<T> maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder<T> delayMillis(long delayMillis) {
            this.delayMillis = delayMillis;
            return this;
        }

        @SafeVarargs
        public final Builder<T> retryOn(Class<? extends Exception>... exceptions) {
            this.retryableExceptions = exceptions;
            return this;
        }

        public RetryHelper<T> build() {
            return new RetryHelper<>(this);
        }

        public T execute() throws Exception {
            return build().execute();
        }
    }
}
```

**Usage examples:**

```java
// Simple retry with defaults (3 attempts, 1 second delay)
String result = RetryHelper.builder(() -> {
    System.out.println("Attempting operation...");
    return "success";
}).execute();

// Custom configuration
int retries = RetryHelper.builder(() -> apiCall())
    .maxAttempts(5)
    .delayMillis(2000)
    .execute();

// Runnable (no return value)
RetryHelper.builder(() -> {
    connectToDatabase();
})
    .maxAttempts(10)
    .delayMillis(500)
    .execute();

// Retry only on specific exceptions
String data = RetryHelper.builder(() -> fetchData())
    .maxAttempts(4)
    .delayMillis(1500)
    .retryOn(IOException.class, TimeoutException.class)
    .execute();
```

**Assumptions:**
- Default max attempts: 3, default delay: 1000ms
- Retries on all exceptions unless `retryOn()` specifies otherwise
- Last exception is always thrown if all retries are exhausted
- Preserves thread interrupt status if sleep is interrupted