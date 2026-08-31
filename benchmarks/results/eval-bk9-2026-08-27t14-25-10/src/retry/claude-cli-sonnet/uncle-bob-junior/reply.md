Assumptions: "operation" is something that returns a value and may throw a checked exception, so I use the standard `java.util.concurrent.Callable<T>` instead of inventing a new functional interface. `maxAttempts` counts the first try (so `maxAttempts=3` means 1 initial try + 2 retries). The delay is fixed (no backoff) and applied via `Thread.sleep`. Failures are unchecked exceptions so the helper is a drop-in wrapper: `RetryExhaustedException` (wraps the last failure) and `RetryInterruptedException` (thread interrupted while waiting).

```java
package com.postcodeloterij.retry;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Runs an operation, retrying it a fixed number of times with a fixed
 * delay between attempts whenever it throws an exception.
 */
public final class Retryer {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    public Retryer(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    /**
     * Executes {@code operation}, retrying on any exception up to
     * {@code maxAttempts} times in total.
     *
     * @throws RetryExhaustedException if every attempt failed
     * @throws RetryInterruptedException if interrupted while waiting to retry
     */
    public <T> T run(Callable<T> operation) {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
                if (attempt < maxAttempts) {
                    waitBeforeNextAttempt();
                }
            }
        }

        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private void waitBeforeNextAttempt() {
        try {
            Thread.sleep(delayBetweenAttempts.toMillis());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(interrupted);
        }
    }
}
```

```java
package com.postcodeloterij.retry;

/** Thrown when an operation still fails after all retry attempts are used up. */
public final class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int attemptsMade, Throwable lastFailure) {
        super("Operation failed after " + attemptsMade + " attempt(s)", lastFailure);
    }
}
```

```java
package com.postcodeloterij.retry;

/** Thrown when the current thread is interrupted while waiting to retry. */
public final class RetryInterruptedException extends RuntimeException {

    public RetryInterruptedException(InterruptedException cause) {
        super("Interrupted while waiting to retry", cause);
    }
}
```

```java
package com.postcodeloterij.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryerTest {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration SHORT_DELAY = Duration.ofMillis(5);

    @Test
    void returnsResultOnFirstSuccess() {
        Retryer retryer = new Retryer(MAX_ATTEMPTS, SHORT_DELAY);
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.run(() -> {
            calls.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, calls.get());
    }

    @Test
    void retriesUntilOperationSucceeds() {
        Retryer retryer = new Retryer(MAX_ATTEMPTS, SHORT_DELAY);
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.run(() -> {
            if (calls.incrementAndGet() < MAX_ATTEMPTS) {
                throw new RuntimeException("not yet");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(MAX_ATTEMPTS, calls.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        Retryer retryer = new Retryer(MAX_ATTEMPTS, SHORT_DELAY);
        AtomicInteger calls = new AtomicInteger();
        RuntimeException failure = new RuntimeException("always fails");

        Callable<String> alwaysFails = () -> {
            calls.incrementAndGet();
            throw failure;
        };

        RetryExhaustedException thrown =
                assertThrows(RetryExhaustedException.class, () -> retryer.run(alwaysFails));

        assertEquals(MAX_ATTEMPTS, calls.get());
        assertEquals(failure, thrown.getCause());
    }

    @Test
    void rejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new Retryer(0, SHORT_DELAY));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class,
                () -> new Retryer(MAX_ATTEMPTS, Duration.ofMillis(-1)));
    }

    @Test
    void stopsAndRestoresInterruptFlagWhenInterruptedDuringWait() throws InterruptedException {
        Retryer retryer = new Retryer(MAX_ATTEMPTS, Duration.ofSeconds(1));
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        AtomicReference<Boolean> interruptFlagInsideThread = new AtomicReference<>();

        Thread worker = new Thread(() -> {
            try {
                retryer.run(() -> {
                    throw new RuntimeException("always fails");
                });
            } catch (Throwable t) {
                thrown.set(t);
            } finally {
                interruptFlagInsideThread.set(Thread.currentThread().isInterrupted());
            }
        });

        worker.start();
        Thread.sleep(20); // let the worker reach its wait
        worker.interrupt();
        worker.join();

        assertInstanceOf(RetryInterruptedException.class, thrown.get());
        assertTrue(interruptFlagInsideThread.get());
        assertFalse(worker.isAlive());
    }
}
```

`[Retryer + RetryExhaustedException + RetryInterruptedException + RetryerTest]` → cleaned: fixed-count retry loop isolated behind one small class using the standard `Callable<T>`, failure and interruption modeled as distinct unchecked exceptions instead of a generic `RuntimeException`; safe to change because callers can add jitter/backoff later by only touching `waitBeforeNextAttempt()`, and the test suite locks in success-first-try, success-after-retry, exhaustion, invalid-config, and interrupt behavior.