import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RetryTest {

    private static final Duration NO_DELAY = Duration.ZERO;
    private static final Duration TEST_DELAY = Duration.ofMillis(100);

    private final List<Duration> recordedSleeps = new ArrayList<>();
    private final Retry.Sleeper recordingSleeper = recordedSleeps::add;

    @Test
    void returnsResultOnFirstSuccessWithoutWaiting() throws Exception {
        Retry retry = new Retry(3, TEST_DELAY, recordingSleeper);

        String result = retry.execute(() -> "ok");

        assertEquals("ok", result);
        assertTrue(recordedSleeps.isEmpty());
    }

    @Test
    void retriesUntilSuccessAndWaitsBetweenAttempts() throws Exception {
        Retry retry = new Retry(3, TEST_DELAY, recordingSleeper);
        AtomicInteger attempts = new AtomicInteger();

        String result = retry.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IllegalStateException("not yet");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, attempts.get());
        assertEquals(List.of(TEST_DELAY, TEST_DELAY), recordedSleeps);
    }

    @Test
    void throwsLastFailureWithEarlierFailuresSuppressed() {
        Retry retry = new Retry(2, NO_DELAY, recordingSleeper);
        IllegalStateException first = new IllegalStateException("first");
        IllegalStateException second = new IllegalStateException("second");
        AtomicInteger attempts = new AtomicInteger();

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> retry.execute(() -> {
                    throw attempts.incrementAndGet() == 1 ? first : second;
                }));

        assertSame(second, thrown);
        assertEquals(List.of(first), List.of(thrown.getSuppressed()));
    }

    @Test
    void singleAttemptFailureIsThrownWithoutWaiting() {
        Retry retry = new Retry(1, TEST_DELAY, recordingSleeper);

        assertThrows(IllegalStateException.class, () -> retry.execute(() -> {
            throw new IllegalStateException("boom");
        }));
        assertTrue(recordedSleeps.isEmpty());
    }

    @Test
    void interruptionDuringWaitStopsRetryingAndRestoresFlag() {
        Retry retry = new Retry(3, TEST_DELAY, duration -> {
            throw new InterruptedException("interrupted");
        });

        assertThrows(InterruptedException.class, () -> retry.execute(() -> {
            throw new IllegalStateException("fails once");
        }));
        assertTrue(Thread.interrupted(), "interrupt flag must be restored");
    }

    @Test
    void executeVoidRetriesLikeExecute() throws Exception {
        Retry retry = new Retry(2, NO_DELAY, recordingSleeper);
        AtomicInteger attempts = new AtomicInteger();

        retry.executeVoid(() -> {
            if (attempts.incrementAndGet() < 2) {
                throw new IllegalStateException("not yet");
            }
        });

        assertEquals(2, attempts.get());
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new Retry(0, NO_DELAY));
        assertThrows(IllegalArgumentException.class,
                () -> new Retry(1, Duration.ofMillis(-1)));
    }
}
