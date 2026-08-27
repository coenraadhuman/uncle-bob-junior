I'll create a clean, reusable retry helper with configurable attempts and delay. Here's the complete solution:

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

    public <T> T execute(Operation<T> operation) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    sleep();
                }
            }
        }
        
        throw new RetryExhaustedException(
            "Failed after " + maxAttempts + " attempts",
            lastException
        );
    }

    private void sleep() {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException("Retry sleep interrupted", e);
        }
    }

    @FunctionalInterface
    public interface Operation<T> {
        T run() throws Exception;
    }

    public static class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class RetryInterruptedException extends RuntimeException {
        public RetryInterruptedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
```

**Usage examples:**

```java
public class RetryHelperUsageExample {
    public static void main(String[] args) {
        RetryHelper retryHelper = new RetryHelper(3, 1000);

        // Example 1: Operation that returns a value
        String result = retryHelper.execute(() -> {
            System.out.println("Attempting API call...");
            return callExternalApi();
        });

        // Example 2: Operation with side effects (no return value)
        retryHelper.execute(() -> {
            System.out.println("Attempting database write...");
            writeToDatabase();
            return null;
        });
    }

    private static String callExternalApi() throws IOException {
        // Simulated API call
        return "success";
    }

    private static void writeToDatabase() throws SQLException {
        // Simulated database operation
    }
}
```

**Tests:**

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RetryHelperTest {
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MILLIS = 10;

    @Test
    void executeSucceedsOnFirstAttempt() {
        RetryHelper helper = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        
        String result = helper.execute(() -> "success");
        
        assertEquals("success", result);
    }

    @Test
    void executeRetriesUntilSuccess() {
        RetryHelper helper = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        int[] attemptCount = {0};
        
        String result = helper.execute(() -> {
            attemptCount[0]++;
            if (attemptCount[0] < 3) {
                throw new IOException("Temporary failure");
            }
            return "success";
        });
        
        assertEquals("success", result);
        assertEquals(3, attemptCount[0]);
    }

    @Test
    void executeThrowsRetryExhaustedWhenAllAttemptsFail() {
        RetryHelper helper = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        
        RetryHelper.RetryExhaustedException exception = assertThrows(
            RetryHelper.RetryExhaustedException.class,
            () -> helper.execute(() -> {
                throw new IOException("Persistent failure");
            })
        );
        
        assertTrue(exception.getMessage().contains("Failed after 3 attempts"));
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void executeRespectMaxAttempts() {
        RetryHelper helper = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        int[] attemptCount = {0};
        
        assertThrows(
            RetryHelper.RetryExhaustedException.class,
            () -> helper.execute(() -> {
                attemptCount[0]++;
                throw new IOException("Persistent failure");
            })
        );
        
        assertEquals(MAX_ATTEMPTS, attemptCount[0]);
    }

    @Test
    void invalidMaxAttemptsThrowsIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new RetryHelper(0, DELAY_MILLIS)
        );
    }

    @Test
    void negativeDelayThrowsIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new RetryHelper(MAX_ATTEMPTS, -1)
        );
    }

    @Test
    void interruptedSleepThrowsRetryInterruptedException() {
        RetryHelper helper = new RetryHelper(MAX_ATTEMPTS, 1000);
        int[] attemptCount = {0};
        
        RetryHelper.RetryInterruptedException exception = assertThrows(
            RetryHelper.RetryInterruptedException.class,
            () -> helper.execute(() -> {
                if (++attemptCount[0] == 1) {
                    Thread.currentThread().interrupt();
                    throw new IOException("First attempt fails");
                }
                return "success";
            })
        );
        
        assertTrue(exception.getMessage().contains("Retry sleep interrupted"));
    }
}
```

**Assumptions:**
- Java 8+ for lambda expressions
- Unchecked exceptions: any exception is retried
- `Thread.sleep()` for delays; respects interruption and restores interrupt flag
- Guardian validations prevent invalid state (negative attempts/delay)
- Generic `<T>` supports any return type; use `null` for void-like operations

Cleaned: intent-revealing names (`maxAttempts`, `delayMillis`, `Operation`), guard clauses for invalid inputs, early return on success, separate exception types for different failure modes. Safe to change because the interface is stable and behavior is fully tested.