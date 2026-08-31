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
