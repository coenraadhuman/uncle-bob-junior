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
