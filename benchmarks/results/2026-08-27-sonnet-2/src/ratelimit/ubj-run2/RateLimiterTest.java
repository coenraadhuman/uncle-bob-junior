// RateLimiterTest.java
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    @Test
    void allowsRequestsUpToTheLimit() {
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS, WINDOW, Clock.systemUTC());

        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.tryAcquire("client-a"));
        }
    }

    @Test
    void blocksRequestsBeyondTheLimit() {
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS, WINDOW, Clock.systemUTC());

        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.tryAcquire("client-a");
        }

        assertFalse(rateLimiter.tryAcquire("client-a"));
    }

    @Test
    void tracksEachClientIndependently() {
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS, WINDOW, Clock.systemUTC());

        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.tryAcquire("client-a");
        }

        assertTrue(rateLimiter.tryAcquire("client-b"));
    }

    @Test
    void resetsTheLimitAfterTheWindowElapses() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS, WINDOW, clock);

        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.tryAcquire("client-a");
        }
        assertFalse(rateLimiter.tryAcquire("client-a"));

        clock.advanceBy(WINDOW.plusSeconds(1));

        assertTrue(rateLimiter.tryAcquire("client-a"));
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advanceBy(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.systemDefault();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException("not needed for tests");
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
