// RetryHelperTest.java
package retry;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryHelperTest {

    private static final Duration DELAY = Duration.ofMillis(50);

    private static final class RecordingSleeper implements Sleeper {
        final List<Duration> calls = new ArrayList<>();

        @Override
        public void sleep(Duration duration) {
            calls.add(duration);
        }
    }

    private static final class InterruptingSleeper implements Sleeper {
        @Override
        public void sleep(Duration duration) throws InterruptedException {
            throw new InterruptedException("simulated interruption");
        }
    }

    @Test
    void returnsResultWithoutRetryingOnFirstSuccess() {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryHelper retryHelper = new RetryHelper(3, DELAY, sleeper);

        String result = retryHelper.run(() -> "ok");

        assertEquals("ok", result);
        assertTrue(sleeper.calls.isEmpty());
    }

    @Test
    void retriesUntilOperationSucceeds() {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryHelper retryHelper = new RetryHelper(5, DELAY, sleeper);
        AtomicInteger callCount = new AtomicInteger();

        String result = retryHelper.run(() -> {
            if (callCount.incrementAndGet() < 3) {
                throw new RuntimeException("not yet");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, callCount.get());
        assertEquals(List.of(DELAY, DELAY), sleeper.calls);
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryHelper retryHelper = new RetryHelper(3, DELAY, sleeper);
        RuntimeException failure = new RuntimeException("always fails");

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                retryHelper.run(() -> {
                    throw failure;
                }));

        assertEquals(3, thrown.getAttempts());
        assertSame(failure, thrown.getCause());
        assertEquals(2, sleeper.calls.size());
    }

    @Test
    void retriesVoidActionsToo() {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryHelper retryHelper = new RetryHelper(2, DELAY, sleeper);
        AtomicInteger callCount = new AtomicInteger();

        retryHelper.run(() -> {
            if (callCount.incrementAndGet() < 2) {
                throw new RuntimeException("not yet");
            }
        });

        assertEquals(2, callCount.get());
    }

    @Test
    void abortsAndRestoresInterruptFlagWhenSleepIsInterrupted() {
        RetryHelper retryHelper = new RetryHelper(5, DELAY, new InterruptingSleeper());

        assertThrows(RetryInterruptedException.class, () ->
                retryHelper.run(() -> {
                    throw new RuntimeException("always fails");
                }));

        assertTrue(Thread.interrupted(), "interrupt flag should have been restored");
    }

    @Test
    void rejectsNonPositiveMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(0, DELAY));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(3, Duration.ofMillis(-1)));
    }
}
