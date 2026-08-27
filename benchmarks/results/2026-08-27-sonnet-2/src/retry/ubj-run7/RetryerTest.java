package com.example.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryerTest {

    @Test
    void returnsResultWhenOperationSucceedsOnFirstAttempt() throws InterruptedException {
        RetryPolicy policy = RetryPolicy.of(3, Duration.ZERO);

        String result = Retryer.execute(() -> "ok", policy);

        assertEquals("ok", result);
    }

    @Test
    void retriesUntilOperationEventuallySucceeds() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger(0);
        RetryPolicy policy = RetryPolicy.of(5, Duration.ZERO);

        String result = Retryer.execute(() -> {
            int attempt = callCount.incrementAndGet();
            if (attempt < 3) {
                throw new RuntimeException("not yet");
            }
            return "ok";
        }, policy);

        assertEquals("ok", result);
        assertEquals(3, callCount.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttemptsWithLastCausePreserved() {
        AtomicInteger callCount = new AtomicInteger(0);
        RetryPolicy policy = RetryPolicy.of(3, Duration.ZERO);
        RuntimeException finalFailure = new RuntimeException("attempt 3 failed");

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                Retryer.execute(() -> {
                    int attempt = callCount.incrementAndGet();
                    if (attempt == 3) {
                        throw finalFailure;
                    }
                    throw new RuntimeException("attempt " + attempt + " failed");
                }, policy));

        assertEquals(3, callCount.get());
        assertEquals(3, thrown.attemptsMade());
        assertEquals(finalFailure, thrown.getCause());
    }

    @Test
    void doesNotRetryWhenMaxAttemptsIsOne() {
        AtomicInteger callCount = new AtomicInteger(0);
        RetryPolicy policy = RetryPolicy.of(1, Duration.ZERO);

        assertThrows(RetryExhaustedException.class, () ->
                Retryer.execute(() -> {
                    callCount.incrementAndGet();
                    throw new RuntimeException("boom");
                }, policy));

        assertEquals(1, callCount.get());
    }

    @Test
    void waitsFixedDelayBetweenAttempts() throws InterruptedException {
        Duration delay = Duration.ofMillis(50);
        RetryPolicy policy = RetryPolicy.of(3, delay);
        AtomicInteger callCount = new AtomicInteger(0);

        long start = System.nanoTime();
        assertThrows(RetryExhaustedException.class, () ->
                Retryer.execute(() -> {
                    callCount.incrementAndGet();
                    throw new RuntimeException("boom");
                }, policy));
        long elapsedMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();

        // 3 attempts -> 2 delays between them, no delay after the last attempt.
        assertTrue(elapsedMillis >= delay.toMillis() * 2);
    }

    @Test
    void rejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(0, Duration.ZERO));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(3, Duration.ofMillis(-1)));
    }
}
