package com.example.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    private static final int MAX_REQUESTS_PER_WINDOW = 3;
    private static final Duration WINDOW_DURATION = Duration.ofSeconds(60);

    @Test
    void allowsRequestsUpToTheLimit() {
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW_DURATION, new MutableClock(Instant.EPOCH));

        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            assertTrue(rateLimiter.isRequestAllowed("client-a"));
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimitWithinTheSameWindow() {
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW_DURATION, new MutableClock(Instant.EPOCH));

        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.isRequestAllowed("client-a");
        }

        assertFalse(rateLimiter.isRequestAllowed("client-a"));
    }

    @Test
    void allowsRequestsAgainAfterTheWindowElapses() {
        MutableClock clock = new MutableClock(Instant.EPOCH);
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW_DURATION, clock);

        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.isRequestAllowed("client-a");
        }
        assertFalse(rateLimiter.isRequestAllowed("client-a"));

        clock.advanceBy(WINDOW_DURATION.plusSeconds(1));

        assertTrue(rateLimiter.isRequestAllowed("client-a"));
    }

    @Test
    void tracksEachClientIndependently() {
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW_DURATION, new MutableClock(Instant.EPOCH));

        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.isRequestAllowed("client-a");
        }

        assertTrue(rateLimiter.isRequestAllowed("client-b"));
    }

    private static final class MutableClock extends Clock {
        private Instant currentInstant;

        MutableClock(Instant startInstant) {
            this.currentInstant = startInstant;
        }

        void advanceBy(Duration duration) {
            currentInstant = currentInstant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.systemDefault();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }
    }
}
