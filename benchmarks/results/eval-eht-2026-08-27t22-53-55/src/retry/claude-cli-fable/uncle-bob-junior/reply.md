```java
import java.time.Duration;
import java.util.Objects;

/**
 * Runs an operation and retries it on failure, waiting a fixed delay
 * between attempts.
 *
 * Contract: retries on any {@link Exception} (not {@link Error}); after the
 * final failed attempt the last exception is rethrown unchanged. No delay is
 * applied after the final attempt. If the waiting thread is interrupted, the
 * {@link InterruptedException} propagates immediately and no further attempt
 * is made.
 */
public final class Retry {

    /** An operation that may fail with a checked exception. */
    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    /** A void operation that may fail with a checked exception. */
    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }

    /** Seam for waiting, so tests never sleep for real. */
    @FunctionalInterface
    interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    private static final int MINIMUM_ATTEMPTS = 1;

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;
    private final Sleeper sleeper;

    public static Retry of(int maxAttempts, Duration delayBetweenAttempts) {
        return new Retry(maxAttempts, delayBetweenAttempts, duration -> Thread.sleep(duration.toMillis()));
    }

    Retry(int maxAttempts, Duration delayBetweenAttempts, Sleeper sleeper) {
        if (maxAttempts < MINIMUM_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be at least " + MINIMUM_ATTEMPTS + ", was " + maxAttempts);
        }
        Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative, was " + delayBetweenAttempts);
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    /** Runs the operation, retrying on failure; returns its result or rethrows the last exception. */
    public <T> T execute(CheckedSupplier<T> operation) throws Exception {
        Objects.requireNonNull(operation, "operation");
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception failure) {
                lastFailure = failure;
                if (attempt < maxAttempts) {
                    sleeper.sleep(delayBetweenAttempts);
                }
            }
        }
        throw lastFailure;
    }

    /** Runs a void operation with the same retry behaviour as {@link #execute(CheckedSupplier)}. */
    public void execute(CheckedRunnable operation) throws Exception {
        Objects.requireNonNull(operation, "operation");
        execute(() -> {
            operation.run();
            return null;
        });
    }
}
```

```java
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
```

Assumptions: retries on any `Exception` (not `Error`), rethrows the last exception unchanged after exhaustion, no delay after the final attempt, and `InterruptedException` during the delay aborts retrying.

Cleaned: waiting is isolated behind a `Sleeper` seam so tests run instantly and deterministically, configuration is validated once in the constructor and immutable. Safe to change because the retry loop, delay policy, and clock are decoupled; swapping in exponential backoff later touches only the sleep call.