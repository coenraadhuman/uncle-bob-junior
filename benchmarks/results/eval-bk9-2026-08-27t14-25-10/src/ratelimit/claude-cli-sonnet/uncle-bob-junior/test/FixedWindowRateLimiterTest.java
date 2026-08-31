package com.example.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowRateLimiterTest {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT_A = "client-a";
    private static final String CLIENT_B = "client-b";

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private final FixedWindowRateLimiter rateLimiter =
            new FixedWindowRateLimiter(MAX_REQUESTS, WINDOW, clock);

    @Test
    void allowsRequestsUpToTheLimitWithinAWindow() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.allowRequest(CLIENT_A));
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimitWithinAWindow() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.allowRequest(CLIENT_A);
        }

        assertFalse(rateLimiter.allowRequest(CLIENT_A));
    }

    @Test
    void resetsTheLimitOnceTheWindowExpires() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.allowRequest(CLIENT_A);
        }
        clock.advanceBy(WINDOW.plusSeconds(1));

        assertTrue(rateLimiter.allowRequest(CLIENT_A));
    }

    @Test
    void tracksEachClientIndependently() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.allowRequest(CLIENT_A);
        }

        assertTrue(rateLimiter.allowRequest(CLIENT_B));
    }

    private static final class MutableClock extends Clock {
        private Instant currentInstant;

        MutableClock(Instant startingInstant) {
            this.currentInstant = startingInstant;
        }

        void advanceBy(Duration duration) {
            currentInstant = currentInstant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
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
