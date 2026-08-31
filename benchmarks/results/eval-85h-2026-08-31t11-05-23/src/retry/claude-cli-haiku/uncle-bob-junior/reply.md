I'll create a generic retry helper with a functional interface for operations and tests covering success, retry, and failure scenarios.

```java
@FunctionalInterface
public interface Operation<T> {
    T execute() throws Exception;
}
```

```java
public class RetryHelper {
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MILLIS = 1000;

    public static <T> T executeWithRetry(Operation<T> operation, int maxAttempts, long delayMillis) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        throw lastException;
    }

    public static <T> T executeWithRetry(Operation<T> operation, int maxAttempts) throws Exception {
        return executeWithRetry(operation, maxAttempts, DEFAULT_DELAY_MILLIS);
    }

    public static <T> T executeWithRetry(Operation<T> operation) throws Exception {
        return executeWithRetry(operation, DEFAULT_MAX_ATTEMPTS);
    }
}
```

```java
public class RetryHelperTest {
    @Test
    public void successOnFirstAttempt() throws Exception {
        Operation<String> operation = () -> "success";
        String result = RetryHelper.executeWithRetry(operation);
        assertEquals("success", result);
    }

    @Test
    public void successAfterRetry() throws Exception {
        AtomicInteger attemptCount = new AtomicInteger(0);
        Operation<String> operation = () -> {
            attemptCount.incrementAndGet();
            if (attemptCount.get() < 3) {
                throw new RuntimeException("Temporary failure");
            }
            return "success";
        };

        String result = RetryHelper.executeWithRetry(operation, 3, 10);
        assertEquals("success", result);
        assertEquals(3, attemptCount.get());
    }

    @Test
    public void failureAfterMaxAttempts() throws Exception {
        Operation<String> operation = () -> {
            throw new RuntimeException("Always fails");
        };

        assertThrows(RuntimeException.class, () -> 
            RetryHelper.executeWithRetry(operation, 2, 10));
    }

    @Test
    public void respectsDelayBetweenAttempts() throws Exception {
        AtomicInteger attemptCount = new AtomicInteger(0);
        Operation<String> operation = () -> {
            attemptCount.incrementAndGet();
            if (attemptCount.get() < 2) {
                throw new RuntimeException("Fail");
            }
            return "success";
        };

        long start = System.currentTimeMillis();
        RetryHelper.executeWithRetry(operation, 2, 50);
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed >= 50, "Should wait between attempts");
    }
}
```

**Cleaned:** Single responsibility—one functional interface, one generic retry method with named constants for defaults and guard clause to skip delay after final attempt. Safe to change because tests verify the retry count, success after failures, exception propagation, and delay timing.

**Assumptions:** Generic return type `<T>` supports any operation; `Operation` interface mirrors `Callable` but declares `throws Exception` for clarity; overloads provide defaults (3 attempts, 1 second delay); delay only occurs between attempts, not after final failure.