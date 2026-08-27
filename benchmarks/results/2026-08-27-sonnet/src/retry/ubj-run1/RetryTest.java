// RetryTest.java
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryTest {

    @Test
    void returnsResultWithoutRetryingOnFirstSuccess() throws Exception {
        String result = Retry.execute(() -> "ok", 3, Duration.ZERO);

        assertEquals("ok", result);
    }

    @Test
    void retriesUntilOperationSucceeds() throws Exception {
        AtomicInteger attempts = new AtomicInteger();

        String result = Retry.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("transient failure");
            }
            return "ok";
        }, 5, Duration.ZERO);

        assertEquals("ok", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttemptsAllFail() {
        AtomicInteger attempts = new AtomicInteger();

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                Retry.execute(() -> {
                    attempts.incrementAndGet();
                    throw new RuntimeException("always fails");
                }, 3, Duration.ZERO));

        assertEquals(3, attempts.get());
        assertNotNull(thrown.getCause());
    }

    @Test
    void rejectsMaxAttemptsLessThanOne() {
        assertThrows(IllegalArgumentException.class, () ->
                Retry.execute(() -> "ok", 0, Duration.ZERO));
    }

    @Test
    void waitsFixedDelayBetweenAttemptsButNotAfterLastOne() {
        Duration delay = Duration.ofMillis(50);
        long start = System.nanoTime();

        assertThrows(RetryExhaustedException.class, () ->
                Retry.execute(() -> {
                    throw new RuntimeException("fail");
                }, 3, delay));

        long elapsedMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();
        assertTrue(elapsedMillis >= delay.toMillis() * 2);
    }
}
