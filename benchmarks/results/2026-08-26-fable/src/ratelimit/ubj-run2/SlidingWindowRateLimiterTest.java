import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SlidingWindowRateLimiterTest {

    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT = "203.0.113.10";

    private final MutableClock clock = new MutableClock(Instant.parse("2026-08-26T12:00:00Z"));
    private final SlidingWindowRateLimiter limiter =
            new SlidingWindowRateLimiter(LIMIT, WINDOW, clock);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire(CLIENT), "request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void rejectsRequestsOverTheLimit() {
        exhaustLimit(CLIENT);
        assertFalse(limiter.tryAcquire(CLIENT));
    }

    @Test
    void allowsAgainOnceTheWindowHasPassed() {
        exhaustLimit(CLIENT);
        clock.advance(WINDOW.plusSeconds(1));
        assertTrue(limiter.tryAcquire(CLIENT));
    }

    @Test
    void limitsEachClientIndependently() {
        exhaustLimit(CLIENT);
        assertTrue(limiter.tryAcquire("198.51.100.7"));
    }

    @Test
    void retryAfterIsZeroWhenNotLimited() {
        assertEquals(0, limiter.retryAfterSeconds(CLIENT));
    }

    @Test
    void retryAfterCountsDownToTheOldestRequestExpiring() {
        exhaustLimit(CLIENT);
        clock.advance(Duration.ofSeconds(20));
        assertEquals(40, limiter.retryAfterSeconds(CLIENT));
    }

    private void exhaustLimit(String clientId) {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire(clientId));
        }
    }

    /** A Clock whose instant is advanced manually by tests. */
    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }
}
