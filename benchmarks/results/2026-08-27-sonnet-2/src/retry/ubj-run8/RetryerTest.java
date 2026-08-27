package com.example.retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryerTest {

    private static final Duration ANY_DELAY = Duration.ofMillis(50);
    private final RecordingSleeper sleeper = new RecordingSleeper();

    @Test
    void returnsResultWithoutRetryingWhenFirstAttemptSucceeds() {
        Retryer retryer = new Retryer(3, ANY_DELAY, sleeper);
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.execute(() -> {
            calls.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, calls.get());
        assertEquals(0, sleeper.sleepCount());
    }

    @Test
    void retriesUntilOperationSucceeds() {
        Retryer retryer = new Retryer(5, ANY_DELAY, sleeper);
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.execute(() -> {
            int attempt = calls.incrementAndGet();
            if (attempt < 3) {
                throw new RuntimeException("attempt " + attempt + " failed");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, calls.get());
        assertEquals(2, sleeper.sleepCount());
    }

    @Test
    void throwsRetryExhaustedExceptionWhenAllAttemptsFail() {
        int maxAttempts = 4;
        Retryer retryer = new Retryer(maxAttempts, ANY_DELAY, sleeper);
        AtomicInteger calls = new AtomicInteger();
        RuntimeException lastFailure = new RuntimeException("boom");

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
            retryer.execute(() -> {
                calls.incrementAndGet();
                throw lastFailure;
            }));

        assertEquals(maxAttempts, calls.get());
        assertSame(lastFailure, thrown.getCause());
        assertEquals(maxAttempts - 1, sleeper.sleepCount());
    }

    @Test
    void waitsFixedDelayBetweenAttempts() {
        Duration delay = Duration.ofMillis(200);
        Retryer retryer = new Retryer(3, delay, sleeper);

        assertThrows(RetryExhaustedException.class, () -> retryer.execute(() -> {
            throw new RuntimeException("boom");
        }));

        assertEquals(List.of(delay, delay), sleeper.recordedDelays());
    }

    @Test
    void rejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new Retryer(0, ANY_DELAY));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new Retryer(3, Duration.ofMillis(-1)));
    }

    @Test
    void restoresInterruptStatusWhenSleepIsInterrupted() {
        Retryer retryer = new Retryer(3, ANY_DELAY, duration -> {
            throw new InterruptedException("interrupted");
        });

        try {
            assertThrows(IllegalStateException.class, () -> retryer.execute(() -> {
                throw new RuntimeException("boom");
            }));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    private static final class RecordingSleeper implements Sleeper {
        private final List<Duration> delays = new ArrayList<>();

        @Override
        public void sleep(Duration duration) {
            delays.add(duration);
        }

        int sleepCount() {
            return delays.size();
        }

        List<Duration> recordedDelays() {
            return delays;
        }
    }
}
