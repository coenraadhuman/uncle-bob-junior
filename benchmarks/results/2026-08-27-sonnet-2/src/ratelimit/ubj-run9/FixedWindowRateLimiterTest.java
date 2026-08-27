// File: src/test/java/com/example/ratelimit/FixedWindowRateLimiterTest.java
package com.example.ratelimit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowRateLimiterTest {

    private static final int MAX_REQUESTS_PER_WINDOW = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private final FixedWindowRateLimiter rateLimiter =
            new FixedWindowRateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW, clock);

    @AfterEach
    void tearDown() {
        rateLimiter.close();
    }

    @Test
    void allowsUpToTheConfiguredLimitWithinAWindow() {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            assertTrue(rateLimiter.tryAcquire("client-a"));
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimitWithinTheSameWindow() {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.tryAcquire("client-b");
        }

        assertFalse(rateLimiter.tryAcquire("client-b"));
    }

    @Test
    void resetsTheCountAfterTheWindowElapses() {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.tryAcquire("client-c");
        }

        clock.advance(WINDOW.plusSeconds(1));

        assertTrue(rateLimiter.tryAcquire("client-c"));
    }

    @Test
    void tracksSeparateClientsIndependently() {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.tryAcquire("client-d");
        }

        assertTrue(rateLimiter.tryAcquire("client-e"));
    }
}
