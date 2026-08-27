import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryerTest {

    @Test
    void returnsResultOnFirstSuccess() {
        Retryer retryer = new Retryer(new RetryConfig(3, Duration.ofMillis(10)), noOpSleeper());
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.execute(() -> {
            calls.incrementAndGet();
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(1, calls.get());
    }

    @Test
    void retriesUntilSuccessWithinMaxAttempts() {
        Retryer retryer = new Retryer(new RetryConfig(3, Duration.ofMillis(10)), noOpSleeper());
        AtomicInteger calls = new AtomicInteger();

        String result = retryer.execute(() -> {
            int attempt = calls.incrementAndGet();
            if (attempt < 3) {
                throw new RuntimeException("fail " + attempt);
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, calls.get());
    }

    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        Retryer retryer = new Retryer(new RetryConfig(2, Duration.ofMillis(10)), noOpSleeper());
        AtomicInteger calls = new AtomicInteger();

        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () ->
                retryer.execute(() -> {
                    calls.incrementAndGet();
                    throw new RuntimeException("always fails");
                }));

        assertEquals(2, calls.get());
        assertEquals("always fails", thrown.getCause().getMessage());
    }

    @Test
    void sleepsBetweenAttemptsButNotAfterTheLastOne() {
        List<Duration> recordedSleeps = new ArrayList<>();
        Retryer retryer = new Retryer(new RetryConfig(3, Duration.ofMillis(50)), recordedSleeps::add);

        assertThrows(RetryExhaustedException.class, () -> retryer.execute(() -> {
            throw new RuntimeException("always fails");
        }));

        assertEquals(List.of(Duration.ofMillis(50), Duration.ofMillis(50)), recordedSleeps);
    }

    @Test
    void rejectsMaxAttemptsBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new RetryConfig(0, Duration.ofMillis(10)));
    }

    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryConfig(3, Duration.ofMillis(-1)));
    }

    private static Sleeper noOpSleeper() {
        return duration -> { };
    }
}
