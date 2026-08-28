Assumptions: attempts and delay are fixed (no backoff), the operation is represented as a `Callable`-style functional interface that may throw a checked exception, and on final failure the caller gets the last exception wrapped in a dedicated unchecked exception (never `null`). Sleeping is isolated behind a `Sleeper` seam so tests don't need real `Thread.sleep` calls and can verify delay behavior deterministically.

`RetryableOperation.java`
```java
package retry;

@FunctionalInterface
public interface RetryableOperation<T> {
    T execute() throws Exception;
}
```

`RetryPolicy.java`
```java
package retry;

import java.time.Duration;

public record RetryPolicy(int maxAttempts, Duration delayBetweenAttempts) {

    public RetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative");
        }
    }
}
```

`RetryExhaustedException.java`
```java
package retry;

public class RetryExhaustedException extends RuntimeException {

    private final int attemptsMade;

    public RetryExhaustedException(int attemptsMade, Exception lastFailure) {
        super("Operation failed after " + attemptsMade + " attempts", lastFailure);
        this.attemptsMade = attemptsMade;
    }

    public int attemptsMade() {
        return attemptsMade;
    }
}
```

`RetryInterruptedException.java`
```java
package retry;

public class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Retry wait was interrupted", cause);
    }
}
```

`Sleeper.java`
```java
package retry;

import java.time.Duration;

@FunctionalInterface
interface Sleeper {
    void sleep(Duration delay) throws InterruptedException;
}
```

`Retryer.java`
```java
package retry;

import java.time.Duration;

public final class Retryer {

    private final Sleeper sleeper;

    public Retryer() {
        this(delay -> Thread.sleep(delay.toMillis()));
    }

    Retryer(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    /**
     * Runs {@code operation}, retrying on any {@link Exception} up to
     * {@code policy.maxAttempts()} times, waiting {@code policy.delayBetweenAttempts()}
     * between attempts (never after the final one).
     *
     * @throws RetryExhaustedException if every attempt failed; its cause is the last failure.
     */
    public <T> T run(RetryableOperation<T> operation, RetryPolicy policy) {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            try {
                return operation.execute();
            } catch (Exception failure) {
                lastFailure = failure;
            }
            if (attempt == policy.maxAttempts()) {
                break;
            }
            sleepBetweenAttempts(policy.delayBetweenAttempts());
        }

        throw new RetryExhaustedException(policy.maxAttempts(), lastFailure);
    }

    private void sleepBetweenAttempts(Duration delay) {
        try {
            sleeper.sleep(delay);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(interruptedException);
        }
    }
}
```

`RetryerTest.java`
```java
package retry;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryerTest {

    private static final RetryPolicy THREE_ATTEMPTS_NO_DELAY =
            new RetryPolicy(3, Duration.ZERO);

    @Test
    void succeedsOnFirstAttempt_doesNotRetry() {
        AtomicInteger callCount = new AtomicInteger();
        Retryer retryer = new Retryer(new RecordingSleeper());

        String result = retryer.run(() -> {
            callCount.incrementAndGet();
            return "ok";
        }, THREE_ATTEMPTS_NO_DELAY);

        assertEquals("ok", result);
        assertEquals(1, callCount.get());
    }

    @Test
    void succeedsAfterTransientFailures_returnsResult() {
        AtomicInteger callCount = new AtomicInteger();
        Retryer retryer = new Retryer(new RecordingSleeper());

        String result = retryer.run(() -> {
            if (callCount.incrementAndGet() < 3) {
                throw new IOException("transient failure");
            }
            return "recovered";
        }, THREE_ATTEMPTS_NO_DELAY);

        assertEquals("recovered", result);
        assertEquals(3, callCount.get());
    }

    @Test
    void exhaustsAllAttempts_throwsRetryExhaustedExceptionWithLastFailure() {
        Retryer retryer = new Retryer(new RecordingSleeper());
        Exception boom = new IllegalStateException("boom");

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                retryer.run(() -> {
                    throw boom;
                }, THREE_ATTEMPTS_NO_DELAY));

        assertEquals(3, thrown.attemptsMade());
        assertEquals(boom, thrown.getCause());
    }

    @Test
    void waitsBetweenAttemptsButNotAfterTheFinalOne() {
        RecordingSleeper sleeper = new RecordingSleeper();
        Retryer retryer = new Retryer(sleeper);
        Duration configuredDelay = Duration.ofMillis(50);

        assertThrows(RetryExhaustedException.class, () ->
                retryer.run(() -> {
                    throw new IOException("always fails");
                }, new RetryPolicy(3, configuredDelay)));

        assertEquals(List.of(configuredDelay, configuredDelay), sleeper.recordedDelays());
    }

    @Test
    void interruptionDuringWait_restoresInterruptFlagAndThrows() {
        Retryer retryer = new Retryer(delay -> {
            throw new InterruptedException("interrupted");
        });

        try {
            assertThrows(RetryInterruptedException.class, () ->
                    retryer.run(() -> {
                        throw new IOException("fails");
                    }, new RetryPolicy(2, Duration.ofMillis(10))));

            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void retryPolicy_rejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(0, Duration.ZERO));
    }

    @Test
    void retryPolicy_rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class,
                () -> new RetryPolicy(3, Duration.ofMillis(-1)));
    }

    private static final class RecordingSleeper implements Sleeper {
        private final List<Duration> recordedDelays = new ArrayList<>();

        @Override
        public void sleep(Duration delay) {
            recordedDelays.add(delay);
        }

        List<Duration> recordedDelays() {
            return recordedDelays;
        }
    }
}
```

`Retryer` → cleaned: retry loop guard-clauses the last-attempt case instead of nesting the delay decision, sleeping is behind a `Sleeper` seam, and `RetryPolicy` makes an invalid attempt/delay unrepresentable. Safe to change because tests cover success, recovery, exhaustion, delay timing, and interruption without any real sleeping.