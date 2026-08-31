// SlidingWindowRateLimiterTest.java
package com.example.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowRateLimiterTest {

    private static final RateLimitConfig CONFIG = new RateLimitConfig(3, Duration.ofMinutes(1));
    private final SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(CONFIG);
    private final Instant start = Instant.parse("2026-08-28T10:00:00Z");

    @Test
    void allowsRequestsUpToLimit() {
        assertTrue(rateLimiter.tryAcquire("client-1", start));
        assertTrue(rateLimiter.tryAcquire("client-1", start.plusSeconds(1)));
        assertTrue(rateLimiter.tryAcquire("client-1", start.plusSeconds(2)));
    }

    @Test
    void deniesRequestBeyondLimitWithinWindow() {
        rateLimiter.tryAcquire("client-1", start);
        rateLimiter.tryAcquire("client-1", start.plusSeconds(1));
        rateLimiter.tryAcquire("client-1", start.plusSeconds(2));

        assertFalse(rateLimiter.tryAcquire("client-1", start.plusSeconds(3)));
    }

    @Test
    void allowsRequestAgainAfterWindowElapses() {
        rateLimiter.tryAcquire("client-1", start);
        rateLimiter.tryAcquire("client-1", start.plusSeconds(1));
        rateLimiter.tryAcquire("client-1", start.plusSeconds(2));

        assertTrue(rateLimiter.tryAcquire("client-1", start.plus(Duration.ofMinutes(1)).plusSeconds(1)));
    }

    @Test
    void tracksClientsIndependently() {
        rateLimiter.tryAcquire("client-1", start);
        rateLimiter.tryAcquire("client-1", start.plusSeconds(1));
        rateLimiter.tryAcquire("client-1", start.plusSeconds(2));

        assertTrue(rateLimiter.tryAcquire("client-2", start.plusSeconds(2)));
    }
}
