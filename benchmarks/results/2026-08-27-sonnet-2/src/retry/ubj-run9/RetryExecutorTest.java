import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryExecutorTest {

    private static final Duration NO_DELAY = Duration.ZERO;

    @Test
    void returnsResultOnFirstSuccessWithoutRetrying() throws InterruptedException {
        RetryExecutor retryExecutor = new RetryExecutor(3, NO_DELAY);
        AtomicInteger callCount = new AtomicInteger();

        String result = retryExecutor.execute(() -> {
            callCount.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, callCount.get());
    }

    @Test
    void retriesUntilSuccessWithinMaxAttempts() throws InterruptedException {
        RetryExecutor retryExecutor = new RetryExecutor(3, NO_DELAY);
        AtomicInteger callCount = new AtomicInteger();

        String result = retryExecutor.execute(() -> {
            if (callCount.incrementAndGet() < 3) {
                throw new RuntimeException("transient failure");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, callCount.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        RetryExecutor retryExecutor = new RetryExecutor(3, NO_DELAY);
        AtomicInteger callCount = new AtomicInteger();

        RetryExecutor.RetryExhaustedException thrown = assertThrows(
                RetryExecutor.RetryExhaustedException.class,
                () -> retryExecutor.execute(() -> {
                    callCount.incrementAndGet();
                    throw new RuntimeException("always fails");
                }));

        assertEquals(3, callCount.get());
        assertEquals("always fails", thrown.getCause().getMessage());
    }

    @Test
    void rejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryExecutor(0, NO_DELAY));
    }
}
