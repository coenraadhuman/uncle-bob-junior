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
