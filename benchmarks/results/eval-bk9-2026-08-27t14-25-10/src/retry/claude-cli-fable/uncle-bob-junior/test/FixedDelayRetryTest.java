import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedDelayRetryTest {

    private static final int THREE_ATTEMPTS = 3;
    private static final Duration DELAY = Duration.ofMillis(50);

    private final List<Duration> recordedSleeps = new ArrayList<>();
    private final FixedDelayRetry retry =
            new FixedDelayRetry(THREE_ATTEMPTS, DELAY, recordedSleeps::add);

    @Test
    void returnsResultOnFirstSuccessWithoutSleeping() throws Exception {
        String result = retry.execute(() -> "ok");

        assertEquals("ok", result);
        assertTrue(recordedSleeps.isEmpty());
    }

    @Test
    void retriesAfterFailureAndWaitsTheFixedDelayBetweenAttempts() throws Exception {
        AtomicInteger attempts = new AtomicInteger();

        String result = retry.execute(() -> {
            if (attempts.incrementAndGet() < THREE_ATTEMPTS) {
                throw new IllegalStateException("transient failure");
            }
            return "recovered";
        });

        assertEquals("recovered", result);
        assertEquals(THREE_ATTEMPTS, attempts.get());
        assertEquals(List.of(DELAY, DELAY), recordedSleeps);
    }

    @Test
    void rethrowsLastFailureWhenAllAttemptsAreExhausted() {
        AtomicInteger attempts = new AtomicInteger();
        IllegalStateException lastFailure = new IllegalStateException("still broken");

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> retry.execute(() -> {
                    attempts.incrementAndGet();
                    throw lastFailure;
                }));

        assertSame(lastFailure, thrown);
        assertEquals(THREE_ATTEMPTS, attempts.get());
        assertEquals(List.of(DELAY, DELAY), recordedSleeps);
    }

    @Test
    void doesNotRetryWhenOperationIsInterrupted() {
        AtomicInteger attempts = new AtomicInteger();

        assertThrows(InterruptedException.class, () -> retry.execute(() -> {
            attempts.incrementAndGet();
            throw new InterruptedException("cancelled");
        }));

        assertEquals(1, attempts.get());
        assertTrue(recordedSleeps.isEmpty());
    }

    @Test
    void singleAttemptMeansNoRetryAndNoSleep() {
        FixedDelayRetry singleAttempt = new FixedDelayRetry(1, DELAY, recordedSleeps::add);

        assertThrows(IllegalStateException.class,
                () -> singleAttempt.execute(() -> { throw new IllegalStateException("boom"); }));
        assertTrue(recordedSleeps.isEmpty());
    }

    @Test
    void allowsZeroDelay() throws Exception {
        FixedDelayRetry zeroDelay = new FixedDelayRetry(2, Duration.ZERO, recordedSleeps::add);
        AtomicInteger attempts = new AtomicInteger();

        String result = zeroDelay.execute(() -> {
            if (attempts.incrementAndGet() == 1) {
                throw new IllegalStateException("first try fails");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(List.of(Duration.ZERO), recordedSleeps);
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new FixedDelayRetry(0, DELAY));
        assertThrows(IllegalArgumentException.class,
                () -> new FixedDelayRetry(2, Duration.ofMillis(-1)));
    }
}
