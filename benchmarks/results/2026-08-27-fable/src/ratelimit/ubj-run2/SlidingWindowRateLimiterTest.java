import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SlidingWindowRateLimiterTest {

    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    /** Test clock that only moves when told to, so the window is deterministic. */
    private static final class ManualClock extends Clock {
        private Instant now = Instant.parse("2026-08-27T12:00:00Z");

        void advance(Duration duration) { now = now.plus(duration); }

        @Override public Instant instant() { return now; }
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
    }

    private final ManualClock clock = new ManualClock();
    private final SlidingWindowRateLimiter limiter =
            new SlidingWindowRateLimiter(LIMIT, WINDOW, clock);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire("client-a"));
        }
    }

    @Test
    void blocksRequestsOverTheLimit() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("client-a");
        }
        assertFalse(limiter.tryAcquire("client-a"));
    }

    @Test
    void limitsClientsIndependently() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("client-a");
        }
        assertTrue(limiter.tryAcquire("client-b"));
    }

    @Test
    void allowsAgainOnceTheWindowSlidesPast() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("client-a");
        }
        clock.advance(WINDOW.plusSeconds(1));
        assertTrue(limiter.tryAcquire("client-a"));
    }

    @Test
    void reportsSecondsUntilRetryWhenBlocked() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("client-a");
        }
        clock.advance(Duration.ofSeconds(20));
        assertEquals(40, limiter.secondsUntilRetry("client-a"));
    }

    @Test
    void reportsZeroRetryDelayForUnknownClient() {
        assertEquals(0, limiter.secondsUntilRetry("client-x"));
    }
}
