// File: RetryerTest.java
package com.example.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryerTest {

    @Test
    void returnsResultOnFirstSuccess() throws Exception {
        Retryer retryer = new Retryer(3, Duration.ofMillis(10));
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.execute(() -> {
            calls.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, calls.get());
    }

    @Test
    void retriesUntilSuccessWithinMaxAttempts() throws Exception {
        Retryer retryer = new Retryer(3, Duration.ofMillis(10));
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.execute(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new RuntimeException("transient failure");
            }
            return "recovered";
        });

        assertEquals("recovered", result);
        assertEquals(3, calls.get());
    }

    @Test
    void throwsRetryExhaustedExceptionWhenAllAttemptsFail() {
        Retryer retryer = new Retryer(3, Duration.ofMillis(10));
        AtomicInteger calls = new AtomicInteger();
        RuntimeException persistentFailure = new RuntimeException("always fails");

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                retryer.execute(() -> {
                    calls.incrementAndGet();
                    throw persistentFailure;
                }));

        assertEquals(3, calls.get());
        assertSame(persistentFailure, thrown.getCause());
    }

    @Test
    void waitsFixedDelayBetweenAttempts() {
        Duration delay = Duration.ofMillis(50);
        Retryer retryer = new Retryer(3, delay);

        long start = System.nanoTime();
        assertThrows(RetryExhaustedException.class, () ->
                retryer.execute(() -> {
                    throw new RuntimeException("fails every time");
                }));
        long elapsedMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();

        assertTrue(elapsedMillis >= delay.toMillis() * 2,
                "expected at least two delays between three attempts");
    }

    @Test
    void rejectsMaxAttemptsBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new Retryer(0, Duration.ofMillis(10)));
    }
}
