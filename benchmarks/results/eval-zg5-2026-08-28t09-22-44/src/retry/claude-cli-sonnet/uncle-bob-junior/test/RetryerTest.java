package com.example.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RetryerTest {

    private static final RetryPolicy THREE_ATTEMPTS_NO_WAIT =
            new RetryPolicy(3, Duration.ZERO);

    private final RecordingSleeper sleeper = new RecordingSleeper();
    private final Retryer retryer = new Retryer(sleeper);

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void succeedsOnFirstAttempt_doesNotRetry() {
        CountingOperation operation = new CountingOperation(0);

        String result = retryer.execute(operation, THREE_ATTEMPTS_NO_WAIT);

        assertEquals("ok", result);
        assertEquals(1, operation.attemptCount());
        assertEquals(0, sleeper.calls().size());
    }

    @Test
    void succeedsAfterTransientFailures_retriesThenReturns() {
        CountingOperation operation = new CountingOperation(2);

        String result = retryer.execute(operation, THREE_ATTEMPTS_NO_WAIT);

        assertEquals("ok", result);
        assertEquals(3, operation.attemptCount());
    }

    @Test
    void exhaustsRetries_throwsRetryExhaustedExceptionWithLastFailure() {
        CountingOperation operation = new CountingOperation(Integer.MAX_VALUE);

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class,
                () -> retryer.execute(operation, THREE_ATTEMPTS_NO_WAIT));

        assertEquals(3, operation.attemptCount());
        assertTrue(thrown.getCause() instanceof IllegalStateException);
    }

    @Test
    void waitsBetweenAttemptsButNotAfterTheLastOne() {
        CountingOperation operation = new CountingOperation(Integer.MAX_VALUE);

        assertThrows(RetryExhaustedException.class,
                () -> retryer.execute(operation, THREE_ATTEMPTS_NO_WAIT));

        assertEquals(2, sleeper.calls().size());
    }

    @Test
    void interruptedDuringWait_wrapsAndRestoresInterruptFlag() {
        RecordingSleeper alwaysInterrupts = new RecordingSleeper(true);
        Retryer interruptibleRetryer = new Retryer(alwaysInterrupts);
        CountingOperation operation = new CountingOperation(Integer.MAX_VALUE);

        assertThrows(RetryInterruptedException.class,
                () -> interruptibleRetryer.execute(operation, THREE_ATTEMPTS_NO_WAIT));

        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void policyRejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class,
                () -> new RetryPolicy(0, Duration.ZERO));
    }

    @Test
    void policyRejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class,
                () -> new RetryPolicy(1, Duration.ofSeconds(-1)));
    }

    /** Fails a fixed number of times, then always succeeds. */
    private static final class CountingOperation implements Callable<String> {
        private final int failuresBeforeSuccess;
        private final AtomicInteger attempts = new AtomicInteger();

        CountingOperation(int failuresBeforeSuccess) {
            this.failuresBeforeSuccess = failuresBeforeSuccess;
        }

        @Override
        public String call() {
            int attempt = attempts.incrementAndGet();
            if (attempt <= failuresBeforeSuccess) {
                throw new IllegalStateException("boom on attempt " + attempt);
            }
            return "ok";
        }

        int attemptCount() {
            return attempts.get();
        }
    }

    /** Records each requested delay instead of actually waiting. */
    private static final class RecordingSleeper implements Sleeper {
        private final List<Duration> calls = new ArrayList<>();
        private final boolean interruptOnSleep;

        RecordingSleeper() {
            this(false);
        }

        RecordingSleeper(boolean interruptOnSleep) {
            this.interruptOnSleep = interruptOnSleep;
        }

        @Override
        public void sleep(Duration delay) throws InterruptedException {
            calls.add(delay);
            if (interruptOnSleep) {
                throw new InterruptedException("simulated interruption");
            }
        }

        List<Duration> calls() {
            return calls;
        }
    }
}
