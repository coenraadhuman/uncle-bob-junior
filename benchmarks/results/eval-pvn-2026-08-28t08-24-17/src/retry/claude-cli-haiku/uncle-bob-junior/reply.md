I'll create a clean, reusable retry helper that handles both value-returning and void operations, with comprehensive tests.

**Assumptions:**
- Uses `java.util.concurrent.Callable` for value-returning operations and `Runnable` for void operations
- Fixed delay between retry attempts
- Throws the last exception encountered if all attempts fail
- Constructor validates that `maxAttempts ≥ 1` and `delayMillis ≥ 0`

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

    public void execute(Runnable operation) throws Exception {
        execute(() -> {
            operation.run();
            return null;
        });
    }
}
```

```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryHelperTest {
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MILLIS = 10;

    @Test
    public void succeedsOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);

        int result = retry.execute(() -> 42);

        assertEquals(42, result);
    }

    @Test
    public void retriesAndSucceedsOnThirdAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        AtomicInteger attempts = new AtomicInteger(0);

        int result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new RuntimeException("Simulated failure");
            }
            return 99;
        });

        assertEquals(99, result);
        assertEquals(3, attempts.get());
    }

    @Test
    public void throwsLastExceptionAfterMaxAttempts() {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        AtomicInteger attempts = new AtomicInteger(0);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("Persistent failure");
            });
        });

        assertEquals("Persistent failure", thrown.getMessage());
        assertEquals(MAX_ATTEMPTS, attempts.get());
    }

    @Test
    public void executesRunnableSuccessfully() throws Exception {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, DELAY_MILLIS);
        AtomicInteger invocations = new AtomicInteger(0);

        retry.execute(() -> invocations.incrementAndGet());

        assertEquals(1, invocations.get());
    }

    @Test
    public void singleAttemptDoesNotRetry() {
        RetryHelper retry = new RetryHelper(1, DELAY_MILLIS);
        AtomicInteger attempts = new AtomicInteger(0);

        assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("Fails immediately");
            });
        });

        assertEquals(1, attempts.get());
    }

    @Test
    public void rejectsMaxAttemptsLessThanOne() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RetryHelper(0, DELAY_MILLIS);
        });
    }

    @Test
    public void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RetryHelper(MAX_ATTEMPTS, -1);
        });
    }

    @Test
    public void acceptsZeroDelay() throws Exception {
        RetryHelper retry = new RetryHelper(MAX_ATTEMPTS, 0);

        int result = retry.execute(() -> 42);

        assertEquals(42, result);
    }
}
```

**Usage examples:**

```java
RetryHelper retry = new RetryHelper(3, 1000);

// Retry a value-returning operation
String result = retry.execute(() -> callApiThatMightFail());

// Retry a void operation
retry.execute(() -> sendEmailThatMightFail());
```

Cleaned: flat control flow with early return on success, named constants for all configuration, constructor validation prevents invalid states, guarded clause avoids sleep on final attempt. Safe to change because retry logic is isolated in one place, tested for success, failure, and edge cases.