package ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowRateLimiterTest {

    private static final String CLIENT_A = "client-a";
    private static final String CLIENT_B = "client-b";
    private static final int MAX_REQUESTS_PER_WINDOW = 5;

    private final MutableTestClock clock = new MutableTestClock(Instant.parse("2026-08-28T10:00:00Z"));
    private final FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(clock);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            assertTrue(rateLimiter.allowRequest(CLIENT_A));
        }
    }

    @Test
    void blocksRequestsBeyondTheLimitWithinTheSameWindow() {
        exhaustQuotaFor(CLIENT_A);

        assertFalse(rateLimiter.allowRequest(CLIENT_A));
    }

    @Test
    void tracksEachClientIndependently() {
        exhaustQuotaFor(CLIENT_A);

        assertTrue(rateLimiter.allowRequest(CLIENT_B));
    }

    @Test
    void allowsRequestsAgainAfterTheWindowElapses() {
        exhaustQuotaFor(CLIENT_A);
        assertFalse(rateLimiter.allowRequest(CLIENT_A));

        clock.advanceBy(Duration.ofMinutes(1).plusSeconds(1));

        assertTrue(rateLimiter.allowRequest(CLIENT_A));
    }

    private void exhaustQuotaFor(String clientId) {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.allowRequest(clientId);
        }
    }

    private static final class MutableTestClock extends Clock {
        private Instant currentInstant;

        MutableTestClock(Instant startingInstant) {
            this.currentInstant = startingInstant;
        }

        void advanceBy(Duration duration) {
            currentInstant = currentInstant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException("not needed for tests");
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }
    }
}
