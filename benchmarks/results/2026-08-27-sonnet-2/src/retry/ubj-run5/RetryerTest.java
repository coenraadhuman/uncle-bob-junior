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

    @Test
    void returnsResultWhenOperationSucceedsFirstTry() {
        AtomicInteger calls = new AtomicInteger();
        RetryPolicy policy = RetryPolicy.of(3, NO_DELAY);

        String result = Retryer.run(() -> {
            calls.incrementAndGet();
            return "ok";
        }, policy);

        assertEquals("ok", result);
        assertEquals(1, calls.get());
    }

    @Test
    void retriesUntilOperationSucceeds() {
        AtomicInteger calls = new AtomicInteger();
        int failuresBeforeSuccess = 2;
        RetryPolicy policy = RetryPolicy.of(5, NO_DELAY);

        String result = Retryer.run(() -> {
            if (calls.incrementAndGet() <= failuresBeforeSuccess) {
                throw new RuntimeException("transient failure");
            }
            return "recovered";
        }, policy);

        assertEquals("recovered", result);
        assertEquals(failuresBeforeSuccess + 1, calls.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        AtomicInteger calls = new AtomicInteger();
        int maxAttempts = 3;
        RetryPolicy policy = RetryPolicy.of(maxAttempts, NO_DELAY);
        RuntimeException persistentFailure = new RuntimeException("always fails");

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                Retryer.run(() -> {
                    calls.incrementAndGet();
                    throw persistentFailure;
                }, policy));

        assertEquals(maxAttempts, calls.get());
        assertEquals(maxAttempts, thrown.attemptsMade());
        assertEquals(persistentFailure, thrown.getCause());
    }

    @Test
    void waitsFixedDelayBetweenAttempts() {
        Duration delay = Duration.ofMillis(50);
        int maxAttempts = 3;
        RetryPolicy policy = RetryPolicy.of(maxAttempts, delay);

        long start = System.nanoTime();
        assertThrows(RetryExhaustedException.class, () ->
                Retryer.run(() -> {
                    throw new RuntimeException("always fails");
                }, policy));
        long elapsedMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();

        long expectedMinimumDelay = delay.toMillis() * (maxAttempts - 1);
        assertTrue(elapsedMillis >= expectedMinimumDelay,
                "expected at least " + expectedMinimumDelay + "ms elapsed, was " + elapsedMillis);
    }

    @Test
    void rejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(0, NO_DELAY));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(3, Duration.ofMillis(-1)));
    }

    @Test
    void wrapsInterruptionAndRestoresInterruptStatus() throws InterruptedException {
        RetryPolicy policy = RetryPolicy.of(2, Duration.ofSeconds(5));
        AtomicInteger interruptedFlagAfterRun = new AtomicInteger(-1);

        Thread worker = new Thread(() -> {
            try {
                Retryer.run(() -> {
                    throw new RuntimeException("fails, forcing a wait");
                }, policy);
            } catch (RetryInterruptedException expected) {
                interruptedFlagAfterRun.set(Thread.currentThread().isInterrupted() ? 1 : 0);
            }
        });
        worker.start();
        Thread.sleep(50);
        worker.interrupt();
        worker.join();

        assertEquals(1, interruptedFlagAfterRun.get());
    }
}
