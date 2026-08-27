package com.plg.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowRateLimiterTest {

    private static final int MAX_REQUESTS_PER_WINDOW = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT_A = "client-a";
    private static final String CLIENT_B = "client-b";

    @Test
    void allowsRequestsUpToTheLimit() {
        SlidingWindowRateLimiter limiter = newLimiter(Instant.parse("2026-08-27T10:00:00Z"));

        assertTrue(limiter.tryAcquire(CLIENT_A));
        assertTrue(limiter.tryAcquire(CLIENT_A));
        assertTrue(limiter.tryAcquire(CLIENT_A));
    }

    @Test
    void rejectsRequestsBeyondTheLimitWithinTheSameWindow() {
        SlidingWindowRateLimiter limiter = newLimiter(Instant.parse("2026-08-27T10:00:00Z"));
        exhaustLimit(limiter, CLIENT_A);

        assertFalse(limiter.tryAcquire(CLIENT_A));
    }

    @Test
    void allowsRequestsAgainAfterTheWindowElapses() {
        MutableClock clock = MutableClock.startingAt(Instant.parse("2026-08-27T10:00:00Z"));
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW, clock);
        exhaustLimit(limiter, CLIENT_A);

        clock.advanceBy(WINDOW.plusSeconds(1));

        assertTrue(limiter.tryAcquire(CLIENT_A));
    }

    @Test
    void tracksSeparateClientsIndependently() {
        SlidingWindowRateLimiter limiter = newLimiter(Instant.parse("2026-08-27T10:00:00Z"));
        exhaustLimit(limiter, CLIENT_A);

        assertTrue(limiter.tryAcquire(CLIENT_B));
    }

    private static SlidingWindowRateLimiter newLimiter(Instant startingInstant) {
        return new SlidingWindowRateLimiter(
                MAX_REQUESTS_PER_WINDOW, WINDOW, MutableClock.startingAt(startingInstant));
    }

    private static void exhaustLimit(SlidingWindowRateLimiter limiter, String clientId) {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            limiter.tryAcquire(clientId);
        }
    }
}
