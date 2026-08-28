// FixedWindowRateLimiterTest.java
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

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT_A = "client-a";
    private static final String CLIENT_B = "client-b";

    @Test
    void allowsRequestsUpToTheLimit() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-28T10:00:00Z"));
        try (FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(MAX_REQUESTS, WINDOW, clock)) {
            assertTrue(limiter.tryAcquire(CLIENT_A));
            assertTrue(limiter.tryAcquire(CLIENT_A));
            assertTrue(limiter.tryAcquire(CLIENT_A));
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimitWithinTheSameWindow() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-28T10:00:00Z"));
        try (FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(MAX_REQUESTS, WINDOW, clock)) {
            for (int i = 0; i < MAX_REQUESTS; i++) {
                limiter.tryAcquire(CLIENT_A);
            }
            assertFalse(limiter.tryAcquire(CLIENT_A));
        }
    }

    @Test
    void resetsTheCountAfterTheWindowElapses() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-28T10:00:00Z"));
        try (FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(MAX_REQUESTS, WINDOW, clock)) {
            for (int i = 0; i < MAX_REQUESTS; i++) {
                limiter.tryAcquire(CLIENT_A);
            }
            clock.advance(WINDOW);
            assertTrue(limiter.tryAcquire(CLIENT_A));
        }
    }

    @Test
    void tracksEachClientIndependently() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-28T10:00:00Z"));
        try (FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(MAX_REQUESTS, WINDOW, clock)) {
            for (int i = 0; i < MAX_REQUESTS; i++) {
                limiter.tryAcquire(CLIENT_A);
            }
            assertTrue(limiter.tryAcquire(CLIENT_B));
        }
    }

    /** Test-only clock whose instant can be advanced deterministically. */
    private static final class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public Instant instant() {
            return instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
