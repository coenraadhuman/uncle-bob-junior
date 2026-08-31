```java
import java.time.Duration;

/**
 * An operation that produces a value and may fail with an exception.
 *
 * @param <T> the type of the result
 */
@FunctionalInterface
public interface RetryableOperation<T> {
    T run() throws Exception;
}
```

```java
import java.time.Duration;
import java.util.Objects;

/**
 * Runs an operation and retries it with a fixed delay between attempts.
 *
 * <p>Any {@link Exception} triggers a retry, except {@link InterruptedException},
 * which is rethrown immediately so cancellation is honoured. When all attempts
 * fail, the last failure is rethrown. Instances are immutable and thread-safe.
 */
public final class FixedDelayRetry {

    /** Boundary for waiting, injectable so tests never really sleep. */
    @FunctionalInterface
    interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    private static final int MINIMUM_ATTEMPTS = 1;

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;
    private final Sleeper sleeper;

    /**
     * @param maxAttempts total number of attempts, at least 1 (1 means no retry)
     * @param delayBetweenAttempts non-negative wait between attempts
     */
    public FixedDelayRetry(int maxAttempts, Duration delayBetweenAttempts) {
        this(maxAttempts, delayBetweenAttempts, duration -> Thread.sleep(duration.toMillis()));
    }

    FixedDelayRetry(int maxAttempts, Duration delayBetweenAttempts, Sleeper sleeper) {
        if (maxAttempts < MINIMUM_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be at least " + MINIMUM_ATTEMPTS
                    + " but was " + maxAttempts);
        }
        Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative but was "
                    + delayBetweenAttempts);
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    /**
     * Runs the operation until it succeeds or all attempts are used.
     *
     * @return the operation's result
     * @throws InterruptedException immediately if the operation or the wait is interrupted
     * @throws Exception the last failure, once all attempts are exhausted
     */
    public <T> T execute(RetryableOperation<T> operation) throws Exception {
        Objects.requireNonNull(operation, "operation");
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.run();
            } catch (InterruptedException interrupted) {
                throw interrupted;
            } catch (Exception failure) {
                lastFailure = failure;
            }
            if (attempt < maxAttempts) {
                sleeper.sleep(delayBetweenAttempts);
            }
        }
        throw lastFailure;
    }
}
```

```java
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
```

Assumptions: Java 17+ with JUnit 5; `maxAttempts` counts total attempts (1 = no retry); any `Exception` triggers a retry except `InterruptedException`, and the last failure is rethrown once attempts run out.

Cleaned: waiting is isolated behind an injectable `Sleeper` boundary, so tests verify retry counts and exact delays without real sleeping. Safe to change because the policy is an immutable value object with configuration validated once at construction, and every branch (first success, recovery, exhaustion, interruption, single attempt, zero delay, invalid config) is pinned by a test.