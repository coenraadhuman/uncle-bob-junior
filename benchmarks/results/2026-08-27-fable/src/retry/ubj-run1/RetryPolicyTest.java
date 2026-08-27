package retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryPolicyTest {

    private static final Duration TEST_DELAY = Duration.ofMillis(100);

    private final List<Duration> recordedSleeps = new ArrayList<>();
    private final RetryPolicy threeAttempts =
            new RetryPolicy(3, TEST_DELAY, recordedSleeps::add);

    @Test
    void returnsResultOnFirstSuccessWithoutWaiting() throws Exception {
        String result = threeAttempts.execute(() -> "ok");

        assertEquals("ok", result);
        assertTrue(recordedSleeps.isEmpty());
    }

    @Test
    void retriesUntilSuccessAndWaitsBetweenAttempts() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        String result = threeAttempts.execute(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new IllegalStateException("attempt " + calls.get() + " failed");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, calls.get());
        assertEquals(List.of(TEST_DELAY, TEST_DELAY), recordedSleeps);
    }

    @Test
    void throwsRetryExhaustedWithLastFailureAsCauseAndEarlierOnesSuppressed() {
        AtomicInteger calls = new AtomicInteger();

        RetryExhaustedException exhausted = assertThrows(RetryExhaustedException.class,
                () -> threeAttempts.execute(() -> {
                    throw new IllegalStateException("failure " + calls.incrementAndGet());
                }));

        assertEquals(3, calls.get());
        assertEquals("failure 3", exhausted.getCause().getMessage());
        assertEquals(1, exhausted.getCause().getSuppressed().length);
    }

    @Test
    void interruptionWhileWaitingAbortsAndRestoresInterruptFlag() {
        RetryPolicy interruptedPolicy = new RetryPolicy(3, TEST_DELAY, duration -> {
            throw new InterruptedException();
        });

        assertThrows(InterruptedException.class,
                () -> interruptedPolicy.execute(() -> {
                    throw new IllegalStateException("first attempt fails");
                }));
    }

    @Test
    void zeroDelayNeverInvokesSleeper() {
        RetryPolicy noDelay = new RetryPolicy(2, Duration.ZERO, recordedSleeps::add);

        assertThrows(RetryExhaustedException.class,
                () -> noDelay.execute(() -> {
                    throw new IllegalStateException("always fails");
                }));
        assertTrue(recordedSleeps.isEmpty());
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(0, TEST_DELAY));
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(1, Duration.ofMillis(-1)));
    }
}
