package com.plg.retry;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryerTest {

    private static final Duration DELAY = Duration.ofMillis(50);

    @Test
    void returnsResultOnFirstSuccess() {
        RecordingSleeper sleeper = new RecordingSleeper();
        Retryer retryer = new Retryer(RetryPolicy.of(3, DELAY), sleeper);

        String result = retryer.run(() -> "ok");

        assertEquals("ok", result);
        assertTrue(sleeper.recordedDelays.isEmpty());
    }

    @Test
    void retriesUntilSuccessAndWaitsBetweenAttempts() {
        RecordingSleeper sleeper = new RecordingSleeper();
        Retryer retryer = new Retryer(RetryPolicy.of(4, DELAY), sleeper);
        AtomicInteger callCount = new AtomicInteger();
        Callable<String> flaky = () -> {
            if (callCount.incrementAndGet() < 3) {
                throw new RuntimeException("not yet");
            }
            return "ok";
        };

        String result = retryer.run(flaky);

        assertEquals("ok", result);
        assertEquals(3, callCount.get());
        assertEquals(List.of(DELAY, DELAY), sleeper.recordedDelays);
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        RecordingSleeper sleeper = new RecordingSleeper();
        Retryer retryer = new Retryer(RetryPolicy.of(3, DELAY), sleeper);
        RuntimeException failure = new RuntimeException("boom");
        Callable<String> alwaysFails = () -> { throw failure; };

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class,
                () -> retryer.run(alwaysFails));

        assertEquals(3, thrown.attemptsMade());
        assertSame(failure, thrown.getCause());
        assertEquals(List.of(DELAY, DELAY), sleeper.recordedDelays);
    }

    @Test
    void wrapsInterruptionAndRestoresInterruptFlag() {
        Sleeper interruptingSleeper = duration -> {
            throw new InterruptedException("stop waiting");
        };
        Retryer retryer = new Retryer(RetryPolicy.of(2, DELAY), interruptingSleeper);
        Callable<String> alwaysFails = () -> { throw new RuntimeException("boom"); };

        try {
            assertThrows(RetryInterruptedException.class, () -> retryer.run(alwaysFails));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Nested
    class RetryPolicyValidation {

        @Test
        void rejectsNonPositiveMaxAttempts() {
            assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(0, DELAY));
        }

        @Test
        void rejectsNegativeDelay() {
            assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(3, Duration.ofMillis(-1)));
        }

        @Test
        void rejectsNullDelay() {
            assertThrows(NullPointerException.class, () -> RetryPolicy.of(3, null));
        }
    }

    private static final class RecordingSleeper implements Sleeper {
        private final List<Duration> recordedDelays = new ArrayList<>();

        @Override
        public void sleep(Duration duration) {
            recordedDelays.add(duration);
        }
    }
}
