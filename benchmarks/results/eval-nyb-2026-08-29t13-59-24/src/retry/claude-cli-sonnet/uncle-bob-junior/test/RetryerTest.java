// RetryerTest.java
package retry;

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

    private static final Duration FIXED_DELAY = Duration.ofMillis(50);

    private final FakeSleeper sleeper = new FakeSleeper();
    private final Retryer retryer = new Retryer(sleeper);

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void returnsResultWithoutRetryingWhenFirstAttemptSucceeds() {
        Callable<String> operation = () -> "ok";

        String result = retryer.execute(operation, RetryPolicy.of(3, FIXED_DELAY));

        assertEquals("ok", result);
        assertEquals(0, sleeper.recordedDelays.size());
    }

    @Test
    void retriesUntilOperationSucceeds() {
        FailingThenSucceedingOperation operation = new FailingThenSucceedingOperation(2, "recovered");

        String result = retryer.execute(operation, RetryPolicy.of(3, FIXED_DELAY));

        assertEquals("recovered", result);
        assertEquals(2, sleeper.recordedDelays.size());
    }

    @Test
    void throwsRetryExhaustedWithLastFailureAfterMaxAttempts() {
        Callable<String> alwaysFails = () -> {
            throw new RuntimeException("boom");
        };

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class,
                () -> retryer.execute(alwaysFails, RetryPolicy.of(3, FIXED_DELAY)));

        assertEquals(3, thrown.attemptsMade());
        assertEquals("boom", thrown.getCause().getMessage());
    }

    @Test
    void waitsFixedDelayBetweenAttemptsButNotAfterFinalFailure() {
        Callable<String> alwaysFails = () -> {
            throw new RuntimeException("boom");
        };

        assertThrows(RetryExhaustedException.class,
                () -> retryer.execute(alwaysFails, RetryPolicy.of(3, FIXED_DELAY)));

        assertEquals(List.of(FIXED_DELAY, FIXED_DELAY), sleeper.recordedDelays);
    }

    @Test
    void restoresInterruptStatusAndStopsRetryingWhenSleepIsInterrupted() {
        sleeper.interruptOnNextSleep = true;
        Callable<String> alwaysFails = () -> {
            throw new RuntimeException("boom");
        };

        assertThrows(RetryInterruptedException.class,
                () -> retryer.execute(alwaysFails, RetryPolicy.of(3, FIXED_DELAY)));

        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void rejectsMaxAttemptsBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(0, FIXED_DELAY));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.of(1, Duration.ofMillis(-1)));
    }

    private static final class FailingThenSucceedingOperation implements Callable<String> {
        private final AtomicInteger remainingFailures;
        private final String successValue;

        FailingThenSucceedingOperation(int failuresBeforeSuccess, String successValue) {
            this.remainingFailures = new AtomicInteger(failuresBeforeSuccess);
            this.successValue = successValue;
        }

        @Override
        public String call() {
            if (remainingFailures.getAndDecrement() > 0) {
                throw new RuntimeException("transient failure");
            }
            return successValue;
        }
    }

    private static final class FakeSleeper implements Sleeper {
        private final List<Duration> recordedDelays = new ArrayList<>();
        private boolean interruptOnNextSleep = false;

        @Override
        public void sleep(Duration duration) throws InterruptedException {
            if (interruptOnNextSleep) {
                interruptOnNextSleep = false;
                throw new InterruptedException("simulated interrupt");
            }
            recordedDelays.add(duration);
        }
    }
}
