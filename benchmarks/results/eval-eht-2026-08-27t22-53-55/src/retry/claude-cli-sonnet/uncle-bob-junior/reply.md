Assumptions: operation is any `Callable<T>` (may throw checked exceptions), delay is fixed (not exponential backoff), and the helper propagates `InterruptedException` rather than swallowing it. Sleeping is injected behind a `Sleeper` seam so tests run instantly instead of actually waiting.

```java
package com.plg.retry;

import java.time.Duration;

@FunctionalInterface
public interface Sleeper {

    Sleeper SYSTEM = duration -> Thread.sleep(duration.toMillis());

    void sleep(Duration duration) throws InterruptedException;
}
```

```java
package com.plg.retry;

import java.time.Duration;
import java.util.Objects;

public final class RetryPolicy {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    public RetryPolicy(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, was " + maxAttempts);
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
    }

    int maxAttempts() {
        return maxAttempts;
    }

    Duration delayBetweenAttempts() {
        return delayBetweenAttempts;
    }
}
```

```java
package com.plg.retry;

public final class RetryExhaustedException extends RuntimeException {

    public RetryExhaustedException(int attemptsMade, Throwable lastFailure) {
        super("Operation failed after " + attemptsMade + " attempt(s)", lastFailure);
    }
}
```

```java
package com.plg.retry;

import java.util.Objects;
import java.util.concurrent.Callable;

public final class Retrier {

    private final RetryPolicy policy;
    private final Sleeper sleeper;

    public Retrier(RetryPolicy policy) {
        this(policy, Sleeper.SYSTEM);
    }

    Retrier(RetryPolicy policy, Sleeper sleeper) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    public <T> T run(Callable<T> operation) throws InterruptedException {
        Throwable lastFailure = null;

        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
                waitBeforeNextAttempt(attempt);
            }
        }

        throw new RetryExhaustedException(policy.maxAttempts(), lastFailure);
    }

    private void waitBeforeNextAttempt(int attemptJustFailed) throws InterruptedException {
        boolean moreAttemptsRemain = attemptJustFailed < policy.maxAttempts();
        if (moreAttemptsRemain) {
            sleeper.sleep(policy.delayBetweenAttempts());
        }
    }
}
```

```java
package com.plg.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetrierTest {

    private static final Duration ANY_DELAY = Duration.ofMillis(50);
    private final RecordingSleeper sleeper = new RecordingSleeper();

    @Test
    void returnsResultWhenOperationSucceedsOnFirstAttempt() throws InterruptedException {
        Retrier retrier = new Retrier(new RetryPolicy(3, ANY_DELAY), sleeper);

        String result = retrier.run(() -> "ok");

        assertEquals("ok", result);
        assertEquals(0, sleeper.sleepCount());
    }

    @Test
    void retriesUntilOperationSucceeds() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger(0);
        Callable<String> operation = () -> {
            if (callCount.incrementAndGet() < 3) {
                throw new RuntimeException("transient failure");
            }
            return "recovered";
        };
        Retrier retrier = new Retrier(new RetryPolicy(3, ANY_DELAY), sleeper);

        String result = retrier.run(operation);

        assertEquals("recovered", result);
        assertEquals(3, callCount.get());
        assertEquals(2, sleeper.sleepCount());
    }

    @Test
    void throwsRetryExhaustedExceptionWhenAllAttemptsFail() {
        Retrier retrier = new Retrier(new RetryPolicy(2, ANY_DELAY), sleeper);
        RuntimeException alwaysFails = new RuntimeException("permanent failure");

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class,
                () -> retrier.run(() -> { throw alwaysFails; }));

        assertSame(alwaysFails, thrown.getCause());
    }

    @Test
    void waitsFixedDelayBetweenAttemptsButNotAfterLastAttempt() {
        Retrier retrier = new Retrier(new RetryPolicy(3, ANY_DELAY), sleeper);

        assertThrows(RetryExhaustedException.class,
                () -> retrier.run(() -> { throw new RuntimeException("fails every time"); }));

        assertEquals(2, sleeper.sleepCount());
        assertEquals(ANY_DELAY, sleeper.lastDelay());
    }

    @Test
    void rejectsPolicyWithFewerThanOneAttempt() {
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(0, ANY_DELAY));
    }

    private static final class RecordingSleeper implements Sleeper {
        private int sleepCount = 0;
        private Duration lastDelay;

        @Override
        public void sleep(Duration duration) {
            sleepCount++;
            lastDelay = duration;
        }

        int sleepCount() {
            return sleepCount;
        }

        Duration lastDelay() {
            return lastDelay;
        }
    }
}
```

→ cleaned: policy is an immutable value object, sleeping sits behind a `Sleeper` seam, and the loop is one flat function per concern; safe to change because callers can swap delay strategy or test timing without touching `Retrier`'s retry logic.