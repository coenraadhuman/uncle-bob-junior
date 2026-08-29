Assumptions: attempts start counting from 1 (so `maxAttempts=3` means the operation runs at most 3 times total), retries happen on any `Exception` (not `Error`), the delay is fixed (not exponential backoff) and is only waited between attempts (never after the final failure), and an interrupt during the wait is treated as fatal rather than silently swallowed.

```java
// RetryPolicy.java
package retry;

import java.time.Duration;

/**
 * Immutable configuration for a retry operation.
 *
 * @param maxAttempts total number of attempts, including the first (must be >= 1)
 */
public final class RetryPolicy {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    private RetryPolicy(int maxAttempts, Duration delayBetweenAttempts) {
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    public static RetryPolicy of(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, was " + maxAttempts);
        }
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException(
                    "delayBetweenAttempts must not be negative, was " + delayBetweenAttempts);
        }
        return new RetryPolicy(maxAttempts, delayBetweenAttempts);
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public Duration delayBetweenAttempts() {
        return delayBetweenAttempts;
    }
}
```

```java
// RetryExhaustedException.java
package retry;

/** Thrown when an operation still fails after all attempts permitted by a {@link RetryPolicy}. */
public final class RetryExhaustedException extends RuntimeException {

    private final int attemptsMade;

    public RetryExhaustedException(int attemptsMade, Throwable lastFailure) {
        super("Operation failed after " + attemptsMade + " attempt(s)", lastFailure);
        this.attemptsMade = attemptsMade;
    }

    public int attemptsMade() {
        return attemptsMade;
    }
}
```

```java
// RetryInterruptedException.java
package retry;

/** Thrown when the thread is interrupted while waiting between retry attempts. */
public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Retry wait was interrupted", cause);
    }
}
```

```java
// Sleeper.java
package retry;

import java.time.Duration;

/** Seam over blocking waits so retry timing can be faked in tests. */
interface Sleeper {
    void sleep(Duration duration) throws InterruptedException;
}
```

```java
// Retryer.java
package retry;

import java.time.Duration;
import java.util.concurrent.Callable;

/** Runs an operation, retrying it on failure according to a {@link RetryPolicy}. */
public final class Retryer {

    private final Sleeper sleeper;

    public Retryer() {
        this(duration -> Thread.sleep(duration.toMillis()));
    }

    Retryer(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    /**
     * @throws RetryExhaustedException if every attempt fails
     * @throws RetryInterruptedException if interrupted while waiting between attempts
     */
    public <T> T execute(Callable<T> operation, RetryPolicy policy) {
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
                sleepUnlessFinalAttempt(attempt, policy);
            }
        }
        throw new RetryExhaustedException(policy.maxAttempts(), lastFailure);
    }

    private void sleepUnlessFinalAttempt(int attempt, RetryPolicy policy) {
        if (attempt == policy.maxAttempts()) {
            return;
        }
        waitBeforeRetry(policy.delayBetweenAttempts());
    }

    private void waitBeforeRetry(Duration delay) {
        try {
            sleeper.sleep(delay);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(interrupted);
        }
    }
}
```

```java
// RetryerTest.java
package retry;

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

    private static final Duration FIXED_DELAY = Duration.ofMillis(50);

    private final FakeSleeper sleeper = new FakeSleeper();
    private final Retryer retryer = new Retryer(sleeper);

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void returnsResultWithoutRetryingWhenFirstAttemptSucceeds() {
        Callable<String> operation = () -> "ok";

        String result = retryer.execute(operation, RetryPolicy.of(3, FIXED_DELAY));

        assertEquals("ok", result);
        assertEquals(0, sleeper.recordedDelays.size());
    }

    @Test
    void retriesUntilOperationSucceeds() {
        FailingThenSucceedingOperation operation = new FailingThenSucceedingOperation(2, "recovered");

        String result = retryer.execute(operation, RetryPolicy.of(3, FIXED_DELAY));

        assertEquals("recovered", result);
        assertEquals(2, sleeper.recordedDelays.size());
    }

    @Test
    void throwsRetryExhaustedWithLastFailureAfterMaxAttempts() {
        Callable<String> alwaysFails = () -> {
            throw new RuntimeException("boom");
        };

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class,
                () -> retryer.execute(alwaysFails, RetryPolicy.of(3, FIXED_DELAY)));

        assertEquals(3, thrown.attemptsMade());
        assertEquals("boom", thrown.getCause().getMessage());
    }

    @Test
    void waitsFixedDelayBetweenAttemptsButNotAfterFinalFailure() {
        Callable<String> alwaysFails = () -> {
            throw new RuntimeException("boom");
        };

        assertThrows(RetryExhaustedException.class,
                () -> retryer.execute(alwaysFails, RetryPolicy.of(3, FIXED_DELAY)));

        assertEquals(List.of(FIXED_DELAY, FIXED_DELAY), sleeper.recordedDelays);
    }

    @Test
    void restoresInterruptStatusAndStopsRetryingWhenSleepIsInterrupted() {
        sleeper.interruptOnNextSleep = true;
        Callable<String> alwaysFails = () -> {
            throw new RuntimeException("boom");
        };

        assertThrows(RetryInterruptedException.class,
                () -> retryer.execute(alwaysFails, RetryPolicy.of(3, FIXED_DELAY)));

        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void rejectsMaxAttemptsBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(0, FIXED_DELAY));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(1, Duration.ofMillis(-1)));
    }

    private static final class FailingThenSucceedingOperation implements Callable<String> {
        private final AtomicInteger remainingFailures;
        private final String successValue;

        FailingThenSucceedingOperation(int failuresBeforeSuccess, String successValue) {
            this.remainingFailures = new AtomicInteger(failuresBeforeSuccess);
            this.successValue = successValue;
        }

        @Override
        public String call() {
            if (remainingFailures.getAndDecrement() > 0) {
                throw new RuntimeException("transient failure");
            }
            return successValue;
        }
    }

    private static final class FakeSleeper implements Sleeper {
        private final List<Duration> recordedDelays = new ArrayList<>();
        private boolean interruptOnNextSleep = false;

        @Override
        public void sleep(Duration duration) throws InterruptedException {
            if (interruptOnNextSleep) {
                interruptOnNextSleep = false;
                throw new InterruptedException("simulated interrupt");
            }
            recordedDelays.add(duration);
        }
    }
}
```

`[code] → cleaned: retry policy, exhaustion/interrupt signalling, and the sleep side effect each live in their own type, sleep hidden behind a `Sleeper` seam → safe to change because tests run instantly with no real waiting and any future backoff strategy only touches `Retryer`.`