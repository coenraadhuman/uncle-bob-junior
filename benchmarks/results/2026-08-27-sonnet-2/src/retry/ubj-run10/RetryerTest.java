// RetryerTest.java
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryerTest {

    private static final Duration FIXED_DELAY = Duration.ofMillis(50);

    @Test
    void succeedsOnFirstAttempt_doesNotSleep() throws Exception {
        RecordingSleeper sleeper = new RecordingSleeper();
        Retryer retryer = new Retryer(sleeper);

        String result = retryer.execute(() -> "ok", new RetryConfig(3, FIXED_DELAY));

        assertEquals("ok", result);
        assertTrue(sleeper.recordedDelays.isEmpty());
    }

    @Test
    void succeedsAfterTransientFailures_returnsResultAndSleepsBetweenAttempts() throws Exception {
        AtomicInteger callCount = new AtomicInteger();
        RecordingSleeper sleeper = new RecordingSleeper();
        Retryer retryer = new Retryer(sleeper);

        String result = retryer.execute(() -> {
            if (callCount.incrementAndGet() < 3) {
                throw new RuntimeException("transient failure");
            }
            return "ok";
        }, new RetryConfig(5, FIXED_DELAY));

        assertEquals("ok", result);
        assertEquals(3, callCount.get());
        assertEquals(List.of(FIXED_DELAY, FIXED_DELAY), sleeper.recordedDelays);
    }

    @Test
    void allAttemptsFail_throwsRetryExhaustedExceptionWithCauseAndCount() {
        RuntimeException failure = new RuntimeException("always fails");
        Retryer retryer = new Retryer(new RecordingSleeper());

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                retryer.execute(() -> {
                    throw failure;
                }, new RetryConfig(3, FIXED_DELAY)));

        assertEquals(3, thrown.attemptsMade());
        assertEquals(failure, thrown.getCause());
    }

    @Test
    void doesNotSleepAfterFinalAttempt() {
        RecordingSleeper sleeper = new RecordingSleeper();
        Retryer retryer = new Retryer(sleeper);

        assertThrows(RetryExhaustedException.class, () ->
                retryer.execute(() -> {
                    throw new RuntimeException("always fails");
                }, new RetryConfig(3, FIXED_DELAY)));

        assertEquals(2, sleeper.recordedDelays.size());
    }

    @Test
    void interruptedWhileWaiting_stopsRetryingAndRestoresInterruptFlag() {
        Sleeper interruptingSleeper = duration -> {
            throw new InterruptedException("simulated interrupt");
        };
        Retryer retryer = new Retryer(interruptingSleeper);
        AtomicInteger callCount = new AtomicInteger();

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                retryer.execute(() -> {
                    callCount.incrementAndGet();
                    throw new RuntimeException("always fails");
                }, new RetryConfig(5, FIXED_DELAY)));

        assertEquals(1, callCount.get());
        assertEquals(1, thrown.attemptsMade());
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted(); // clear flag so it doesn't leak into other tests
    }

    @Test
    void maxAttemptsBelowOne_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> new RetryConfig(0, FIXED_DELAY));
    }

    @Test
    void negativeDelay_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> new RetryConfig(3, Duration.ofMillis(-1)));
    }

    private static final class RecordingSleeper implements Sleeper {
        private final List<Duration> recordedDelays = new ArrayList<>();

        @Override
        public void sleep(Duration duration) {
            recordedDelays.add(duration);
        }
    }
}
