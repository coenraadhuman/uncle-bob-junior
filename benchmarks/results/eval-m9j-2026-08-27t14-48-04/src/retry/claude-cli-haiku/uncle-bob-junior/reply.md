I'll create a reusable retry helper that handles operations with automatic retries and fixed delays. Here are my assumptions:

- **Generic operations**: supports any operation returning a value via `Supplier<T>`
- **All exceptions**: retries on any `Exception` (configurable in extensions)
- **Fail-fast**: throws the last exception when all retries exhaust
- **Thread.sleep for delays**: straightforward, production-ready approach

```java
public class Retry<T> {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLIS = 1000;

    private final int maxAttempts;
    private final long delayMillis;

    public Retry() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MILLIS);
    }

    public Retry(int maxAttempts, long delayMillis) {
        validateAttempts(maxAttempts);
        validateDelay(delayMillis);
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public T execute(Supplier<T> operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                if (shouldRetry(attempt)) {
                    sleep();
                }
            }
        }
        throw lastException;
    }

    private boolean shouldRetry(int currentAttempt) {
        return currentAttempt < maxAttempts;
    }

    private void sleep() throws InterruptedException {
        Thread.sleep(delayMillis);
    }

    private void validateAttempts(int attempts) {
        if (attempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
    }

    private void validateDelay(long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delayMillis cannot be negative");
        }
    }
}
```

**Usage example:**

```java
// Basic usage
Retry<String> retry = new Retry<>(3, 1000);
String result = retry.execute(() -> fetchDataFromApi());

// Custom configuration
Retry<Integer> retryWithBackoff = new Retry<>(5, 2000);
Integer count = retryWithBackoff.execute(() -> queryDatabase());

// Lambda with side effects
Retry<Void> retryVoid = new Retry<>(3, 500);
retryVoid.execute(() -> {
    sendMessage();
    return null;
});
```

**Tests:**

```java
public class RetryTest {
    private Retry<String> retry;

    @Before
    public void setUp() {
        retry = new Retry<>(3, 100);
    }

    @Test
    public void successOnFirstAttempt() throws Exception {
        String result = retry.execute(() -> "success");
        assertEquals("success", result);
    }

    @Test
    public void successAfterRetries() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        String result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new RuntimeException("fail");
            }
            return "success";
        });
        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }

    @Test
    public void throwsAfterMaxAttempts() {
        assertThrows(RuntimeException.class, () ->
            retry.execute(() -> {
                throw new RuntimeException("always fails");
            })
        );
    }

    @Test
    public void invalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new Retry<>(0, 1000));
    }

    @Test
    public void negativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new Retry<>(3, -1));
    }
}
```

**Cleaned: generic operation support with guard-clause error handling, validated inputs, and intent-revealing names; safe to change because each method has one responsibility and all branches are tested.**