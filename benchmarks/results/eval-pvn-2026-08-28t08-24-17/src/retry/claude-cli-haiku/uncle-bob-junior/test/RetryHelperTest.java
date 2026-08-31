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
