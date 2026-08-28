import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryTest {

    private static final Duration DELAY = Duration.ofMillis(100);
    private static final int THREE_ATTEMPTS = 3;

    private final List<Duration> recordedSleeps = new ArrayList<>();
    private final Retry retry = new Retry(THREE_ATTEMPTS, DELAY, recordedSleeps::add);

    @Test
    void returnsResultOnFirstSuccessWithoutSleeping() throws Exception {
        String result = retry.execute(() -> "ok");

        assertEquals("ok", result);
        assertTrue(recordedSleeps.isEmpty());
    }

    @Test
    void retriesUntilSuccessAndSleepsBetweenAttempts() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        String result = retry.execute(() -> {
            if (calls.incrementAndGet() < THREE_ATTEMPTS) {
                throw new IOException("transient failure");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(THREE_ATTEMPTS, calls.get());
        assertEquals(List.of(DELAY, DELAY), recordedSleeps);
    }

    @Test
    void rethrowsLastExceptionWhenAllAttemptsFail() {
        AtomicInteger calls = new AtomicInteger();
        IOException lastFailure = new IOException("final failure");

        IOException thrown = assertThrows(IOException.class, () -> retry.execute(() -> {
            if (calls.incrementAndGet() < THREE_ATTEMPTS) {
                throw new IOException("earlier failure");
            }
            throw lastFailure;
        }));

        assertSame(lastFailure, thrown);
        assertEquals(THREE_ATTEMPTS, calls.get());
    }

    @Test
    void doesNotSleepAfterFinalAttempt() {
        assertThrows(IOException.class, () -> retry.execute(() -> {
            throw new IOException("always fails");
        }));

        assertEquals(THREE_ATTEMPTS - 1, recordedSleeps.size());
    }

    @Test
    void voidOverloadRetriesLikeSupplierOverload() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        retry.execute((Retry.CheckedRunnable) () -> {
            if (calls.incrementAndGet() < 2) {
                throw new IOException("transient failure");
            }
        });

        assertEquals(2, calls.get());
    }

    @Test
    void interruptionDuringDelayAbortsRetrying() {
        Retry interruptedRetry = new Retry(THREE_ATTEMPTS, DELAY, duration -> {
            throw new InterruptedException();
        });
        AtomicInteger calls = new AtomicInteger();

        assertThrows(InterruptedException.class, () -> interruptedRetry.execute(() -> {
            calls.incrementAndGet();
            throw new IOException("failure before interrupt");
        }));

        assertEquals(1, calls.get());
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> Retry.of(0, DELAY));
        assertThrows(IllegalArgumentException.class, () -> Retry.of(1, Duration.ofMillis(-1)));
    }
}
