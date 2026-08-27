// RetryerTest.java
package com.plg.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryerTest {

    private static final Duration NO_DELAY = Duration.ZERO;
    private static final Duration SHORT_DELAY = Duration.ofMillis(20);

    @Test
    void returnsResultWhenOperationSucceedsFirstTry() {
        Retryer retryer = new Retryer();
        RetryConfig config = new RetryConfig(3, NO_DELAY);

        String result = retryer.run(() -> "ok", config);

        assertEquals("ok", result);
    }

    @Test
    void retriesUntilOperationSucceeds() {
        Retryer retryer = new Retryer();
        RetryConfig config = new RetryConfig(5, NO_DELAY);
        AtomicInteger callCount = new AtomicInteger(0);

        String result = retryer.run(() -> {
            int callNumber = callCount.incrementAndGet();
            if (callNumber < 3) {
                throw new RuntimeException("transient failure " + callNumber);
            }
            return "recovered";
        }, config);

        assertEquals("recovered", result);
        assertEquals(3, callCount.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        Retryer retryer = new Retryer();
        RetryConfig config = new RetryConfig(3, NO_DELAY);
        AtomicInteger callCount = new AtomicInteger(0);
        RuntimeException persistentFailure = new RuntimeException("always fails");

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                retryer.run(() -> {
                    callCount.incrementAndGet();
                    throw persistentFailure;
                }, config));

        assertEquals(3, callCount.get());
        assertEquals(persistentFailure, thrown.getCause());
    }

    @Test
    void waitsFixedDelayBetweenAttemptsButNotAfterLast() {
        Retryer retryer = new Retryer();
        int maxAttempts = 3;
        RetryConfig config = new RetryConfig(maxAttempts, SHORT_DELAY);

        long startNanos = System.nanoTime();
        assertThrows(RetryExhaustedException.class, () ->
                retryer.run(() -> {
                    throw new RuntimeException("always fails");
                }, config));
        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;

        long expectedMinimumMillis = SHORT_DELAY.toMillis() * (maxAttempts - 1);
        assertTrue(elapsedMillis >= expectedMinimumMillis,
                "expected at least " + expectedMinimumMillis + "ms, took " + elapsedMillis + "ms");
    }

    @Test
    void rejectsMaxAttemptsBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new RetryConfig(0, NO_DELAY));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryConfig(3, Duration.ofMillis(-1)));
    }

    @Test
    void wrapsInterruptionDuringDelay() throws InterruptedException {
        Retryer retryer = new Retryer();
        RetryConfig config = new RetryConfig(2, Duration.ofSeconds(10));
        AtomicInteger caughtCorrectException = new AtomicInteger(0);

        Thread worker = new Thread(() -> {
            try {
                retryer.run(() -> {
                    throw new RuntimeException("fails so the delay kicks in");
                }, config);
            } catch (RetryInterruptedException expected) {
                caughtCorrectException.set(1);
            }
        });

        worker.start();
        Thread.sleep(50);
        worker.interrupt();
        worker.join();

        assertEquals(1, caughtCorrectException.get());
    }
}
