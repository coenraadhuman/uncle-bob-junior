```java
public class Retry {
    @FunctionalInterface
    public interface Operation<T> {
        T execute() throws Exception;
    }

    @FunctionalInterface
    public interface VoidOperation {
        void execute() throws Exception;
    }

    private final int maxAttempts;
    private final long delayMs;

    public Retry(int maxAttempts, long delayMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }

    public <T> T execute(Operation<T> operation) throws Exception {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
                Thread.sleep(delayMs);
            }
        }
        throw new AssertionError("Unreachable");
    }

    public void executeVoid(VoidOperation operation) throws Exception {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.execute();
                return;
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
                Thread.sleep(delayMs);
            }
        }
    }
}
```

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RetryTest {
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MS = 10;

    private Retry retry;

    @BeforeEach
    public void setUp() {
        retry = new Retry(MAX_ATTEMPTS, DELAY_MS);
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
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("Fail");
            }
            return "success";
        });
        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }

    @Test
    public void throwsAfterMaxAttempts() {
        assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                throw new RuntimeException("Always fails");
            });
        });
    }

    @Test
    public void voidOperationSucceeds() throws Exception {
        AtomicBoolean executed = new AtomicBoolean(false);
        retry.executeVoid(() -> executed.set(true));
        assertTrue(executed.get());
    }

    @Test
    public void voidOperationRetriesAndSucceeds() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        retry.executeVoid(() -> {
            if (attempts.incrementAndGet() < 2) {
                throw new RuntimeException("Fail");
            }
        });
        assertEquals(2, attempts.get());
    }

    @Test
    public void voidOperationThrowsAfterMaxAttempts() {
        assertThrows(RuntimeException.class, () -> {
            retry.executeVoid(() -> {
                throw new RuntimeException("Always fails");
            });
        });
    }

    @Test
    public void waitsDelayBetweenAttempts() throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            retry.execute(() -> {
                throw new RuntimeException("Fail");
            });
        } catch (RuntimeException e) {
            // Expected
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertTrue(elapsedTime >= 20, "Should wait at least 20ms (2 × 10ms delays)");
    }

    @Test
    public void constructorValidatesMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new Retry(0, 10));
    }

    @Test
    public void constructorValidatesDelayMs() {
        assertThrows(IllegalArgumentException.class, () -> new Retry(3, -1));
    }
}
```

**Cleaned:** Retry wraps retry logic in one place with two clearly named variants (execute for results, executeVoid for side effects); guards validate construction; operations throw any exception and are retried up to the configured limit with fixed delay between attempts; all configuration is explicit and immutable.

**Safe to change because:** Tests cover success, failure, retry counting, and timing; logic is centralised so retry semantics change in one place for all callers; no magic values or state.

**Assumptions:** JUnit 5 for tests; maxAttempts ≥ 1, delayMs ≥ 0; retries on any exception (checked or unchecked); fixed delay only—no exponential backoff; blocking delay via Thread.sleep suitable for I/O operations.