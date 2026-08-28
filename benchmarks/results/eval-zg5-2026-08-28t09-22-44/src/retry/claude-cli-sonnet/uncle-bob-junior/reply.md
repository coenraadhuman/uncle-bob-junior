**Assumptions:** operation is a `Callable<T>` (returns a value, may throw a checked exception); delay is fixed and expressed as `java.time.Duration`; failure after all attempts throws an unchecked `RetryExhaustedException` wrapping the last failure (never swallowed); `Thread.sleep` is wrapped behind a `Sleeper` seam so tests don't actually wait; tests use JUnit 5. Since a small in-house retry loop was explicitly requested, I didn't pull in a library like Resilience4j.

```java
package com.example.retry;

import java.time.Duration;
import java.util.Objects;

/**
 * Fixed number of attempts, each separated by a fixed delay.
 *
 * @param maxAttempts total number of attempts, including the first (must be >= 1)
 * @param delay       wait time between attempts (must not be negative)
 */
public record RetryPolicy(int maxAttempts, Duration delay) {

    public RetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        Objects.requireNonNull(delay, "delay");
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative");
        }
    }
}
```

```java
package com.example.retry;

import java.time.Duration;

/** Seam around the blocking wait so tests can avoid real time delays. */
@FunctionalInterface
public interface Sleeper {

    void sleep(Duration delay) throws InterruptedException;
}
```

```java
package com.example.retry;

/** Thrown when an operation still fails after all retry attempts. */
public final class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int attempts, Throwable lastFailure) {
        super("Operation failed after " + attempts + " attempts", lastFailure);
    }
}
```

```java
package com.example.retry;

/** Thrown when the wait between retry attempts is interrupted. */
public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Retry wait was interrupted", cause);
    }
}
```

```java
package com.example.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Runs an operation, retrying on any {@link Exception} up to a fixed number
 * of attempts, waiting a fixed delay between attempts.
 */
public final class Retryer {

    private final Sleeper sleeper;

    public Retryer() {
        this(delay -> Thread.sleep(delay.toMillis()));
    }

    public Retryer(Sleeper sleeper) {
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    /**
     * @throws RetryExhaustedException if every attempt fails
     * @throws RetryInterruptedException if the wait between attempts is interrupted
     */
    public <T> T execute(Callable<T> operation, RetryPolicy policy) {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(policy, "policy");

        Exception lastFailure = null;
        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            if (attempt > 1) {
                waitBeforeRetry(policy.delay());
            }
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
            }
        }
        throw new RetryExhaustedException(policy.maxAttempts(), lastFailure);
    }

    private void waitBeforeRetry(Duration delay) {
        try {
            sleeper.sleep(delay);
        } catch (InterruptedException interruption) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(interruption);
        }
    }
}
```

```java
package com.example.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RetryerTest {

    private static final RetryPolicy THREE_ATTEMPTS_NO_WAIT =
            new RetryPolicy(3, Duration.ZERO);

    private final RecordingSleeper sleeper = new RecordingSleeper();
    private final Retryer retryer = new Retryer(sleeper);

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void succeedsOnFirstAttempt_doesNotRetry() {
        CountingOperation operation = new CountingOperation(0);

        String result = retryer.execute(operation, THREE_ATTEMPTS_NO_WAIT);

        assertEquals("ok", result);
        assertEquals(1, operation.attemptCount());
        assertEquals(0, sleeper.calls().size());
    }

    @Test
    void succeedsAfterTransientFailures_retriesThenReturns() {
        CountingOperation operation = new CountingOperation(2);

        String result = retryer.execute(operation, THREE_ATTEMPTS_NO_WAIT);

        assertEquals("ok", result);
        assertEquals(3, operation.attemptCount());
    }

    @Test
    void exhaustsRetries_throwsRetryExhaustedExceptionWithLastFailure() {
        CountingOperation operation = new CountingOperation(Integer.MAX_VALUE);

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class,
                () -> retryer.execute(operation, THREE_ATTEMPTS_NO_WAIT));

        assertEquals(3, operation.attemptCount());
        assertTrue(thrown.getCause() instanceof IllegalStateException);
    }

    @Test
    void waitsBetweenAttemptsButNotAfterTheLastOne() {
        CountingOperation operation = new CountingOperation(Integer.MAX_VALUE);

        assertThrows(RetryExhaustedException.class,
                () -> retryer.execute(operation, THREE_ATTEMPTS_NO_WAIT));

        assertEquals(2, sleeper.calls().size());
    }

    @Test
    void interruptedDuringWait_wrapsAndRestoresInterruptFlag() {
        RecordingSleeper alwaysInterrupts = new RecordingSleeper(true);
        Retryer interruptibleRetryer = new Retryer(alwaysInterrupts);
        CountingOperation operation = new CountingOperation(Integer.MAX_VALUE);

        assertThrows(RetryInterruptedException.class,
                () -> interruptibleRetryer.execute(operation, THREE_ATTEMPTS_NO_WAIT));

        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void policyRejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class,
                () -> new RetryPolicy(0, Duration.ZERO));
    }

    @Test
    void policyRejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class,
                () -> new RetryPolicy(1, Duration.ofSeconds(-1)));
    }

    /** Fails a fixed number of times, then always succeeds. */
    private static final class CountingOperation implements Callable<String> {
        private final int failuresBeforeSuccess;
        private final AtomicInteger attempts = new AtomicInteger();

        CountingOperation(int failuresBeforeSuccess) {
            this.failuresBeforeSuccess = failuresBeforeSuccess;
        }

        @Override
        public String call() {
            int attempt = attempts.incrementAndGet();
            if (attempt <= failuresBeforeSuccess) {
                throw new IllegalStateException("boom on attempt " + attempt);
            }
            return "ok";
        }

        int attemptCount() {
            return attempts.get();
        }
    }

    /** Records each requested delay instead of actually waiting. */
    private static final class RecordingSleeper implements Sleeper {
        private final List<Duration> calls = new ArrayList<>();
        private final boolean interruptOnSleep;

        RecordingSleeper() {
            this(false);
        }

        RecordingSleeper(boolean interruptOnSleep) {
            this.interruptOnSleep = interruptOnSleep;
        }

        @Override
        public void sleep(Duration delay) throws InterruptedException {
            calls.add(delay);
            if (interruptOnSleep) {
                throw new InterruptedException("simulated interruption");
            }
        }

        List<Duration> calls() {
            return calls;
        }
    }
}
```

`Retryer.execute()` does one job (retry-and-wait), `RetryPolicy` makes invalid attempt counts and negative delays unrepresentable, and `Sleeper` keeps `Thread.sleep` at arm's length so tests run instantly. Callers can swap in a real or fake `Sleeper` without touching retry logic.