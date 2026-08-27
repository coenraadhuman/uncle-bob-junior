package com.example.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    @Test
    void allowsRequestsUpToLimitWithinWindow() {
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW, fixedClock(0));

        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(limiter.tryAcquire("client-a"));
        }
    }

    @Test
    void blocksRequestsExceedingLimitWithinWindow() {
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW, fixedClock(0));

        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.tryAcquire("client-a");
        }

        assertFalse(limiter.tryAcquire("client-a"));
    }

    @Test
    void resetsCountAfterWindowElapses() {
        AtomicLong now = new AtomicLong(0);
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW, now::get);

        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.tryAcquire("client-a");
        }
        assertFalse(limiter.tryAcquire("client-a"));

        now.set(WINDOW.toMillis() + 1);

        assertTrue(limiter.tryAcquire("client-a"));
    }

    @Test
    void tracksEachClientIndependently() {
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW, fixedClock(0));

        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.tryAcquire("client-a");
        }

        assertTrue(limiter.tryAcquire("client-b"));
    }

    @Test
    void evictStaleWindowsRemovesClientsInactiveForTwoWindows() {
        AtomicLong now = new AtomicLong(0);
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS, WINDOW, now::get);
        limiter.tryAcquire("client-a");

        now.set(WINDOW.toMillis() * 2 + 1);
        limiter.evictStaleWindows();

        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(limiter.tryAcquire("client-a"));
        }
    }

    private static LongSupplier fixedClock(long value) {
        return () -> value;
    }
}
