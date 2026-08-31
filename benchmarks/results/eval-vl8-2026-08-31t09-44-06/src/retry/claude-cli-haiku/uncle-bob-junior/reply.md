**Assumptions:**
- Retries apply to any exception (not exception-type-specific)
- Uses `Thread.sleep` for delays (approximately at least that duration)
- Fixed delay between all retry attempts (not exponential backoff)
- Operation represented as a functional interface

```java
public class RetryHelper {
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }

    public static <T> T executeWithRetry(Operation<T> operation) throws Exception {
        return executeWithRetry(operation, MAX_RETRIES, RETRY_DELAY_MS);
    }

    public static <T> T executeWithRetry(Operation<T> operation, int maxAttempts, long delayMs) throws Exception {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
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
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RetryHelperTests {

    @Test
    void executesOperationSuccessfully() throws Exception {
        int result = RetryHelper.executeWithRetry(() -> 42);
        assertEquals(42, result);
    }

    @Test
    void retriesOnFailureThenSucceeds() throws Exception {
        int[] attempts = {0};
        int result = RetryHelper.executeWithRetry(() -> {
            attempts[0]++;
            if (attempts[0] < 3) {
                throw new RuntimeException("Fail");
            }
            return 100;
        }, 3, 10);

        assertEquals(3, attempts[0]);
        assertEquals(100, result);
    }

    @Test
    void throwsAfterMaxRetriesExceeded() {
        assertThrows(RuntimeException.class, () -> {
            RetryHelper.executeWithRetry(() -> {
                throw new RuntimeException("Always fails");
            }, 2, 10);
        });
    }

    @Test
    void appliesDelayBetweenRetries() throws Exception {
        int[] attempts = {0};
        long startTime = System.currentTimeMillis();

        assertThrows(RuntimeException.class, () -> {
            RetryHelper.executeWithRetry(() -> {
                attempts[0]++;
                throw new RuntimeException("Fail");
            }, 3, 50);
        });

        long elapsedMs = System.currentTimeMillis() - startTime;
        assertEquals(3, attempts[0]);
        assertTrue(elapsedMs >= 80, "Should apply delay between retries");
    }

    @Test
    void rejectsZeroMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> {
            RetryHelper.executeWithRetry(() -> 1, 0, 1000);
        });
    }

    @Test
    void preservesLastExceptionOnFailure() {
        RuntimeException expectedException = new RuntimeException("Specific error");
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            RetryHelper.executeWithRetry(() -> {
                throw expectedException;
            }, 2, 10);
        });
        assertSame(expectedException, thrown);
    }

    @Test
    void worksWithCheckedExceptions() throws Exception {
        int[] attempts = {0};
        int result = RetryHelper.executeWithRetry(() -> {
            attempts[0]++;
            if (attempts[0] == 1) {
                throw new InterruptedException("Transient failure");
            }
            return 99;
        }, 3, 10);

        assertEquals(2, attempts[0]);
        assertEquals(99, result);
    }

    @Test
    void returnsNullIfOperationReturnsNull() throws Exception {
        String result = RetryHelper.executeWithRetry(() -> null);
        assertNull(result);
    }
}
```

Cleaned: Guard-clause validation, named constants for max retries and delay (units explicit), flat control flow with early return on success, last exception preserved and rethrown. Safe to change because retry logic is isolated in one place; all paths tested including timing, boundary validation, and exception preservation.