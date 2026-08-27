package com.plg.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    private static final String CLIENT_A = "client-a";
    private static final String CLIENT_B = "client-b";
    private static final int MAX_REQUESTS_PER_MINUTE = 3;

    private final AtomicLong fakeNanoTime = new AtomicLong(0);
    private final RateLimiter rateLimiter = new RateLimiter(
            MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1), fakeNanoTime::get);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int i = 0; i < MAX_REQUESTS_PER_MINUTE; i++) {
            assertTrue(rateLimiter.tryAcquire(CLIENT_A), "request " + i + " should be allowed");
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimit() {
        exhaustLimit(CLIENT_A);

        assertFalse(rateLimiter.tryAcquire(CLIENT_A));
    }

    @Test
    void tracksEachClientIndependently() {
        exhaustLimit(CLIENT_A);

        assertTrue(rateLimiter.tryAcquire(CLIENT_B), "a different client must have its own budget");
    }

    @Test
    void refillsGraduallyAsTimePasses() {
        exhaustLimit(CLIENT_A);
        advanceTimeBy(Duration.ofSeconds(20));

        assertTrue(rateLimiter.tryAcquire(CLIENT_A), "one third of the window should refill one token");
    }

    @Test
    void refillsFullyAfterWindowElapses() {
        exhaustLimit(CLIENT_A);
        advanceTimeBy(Duration.ofMinutes(1));

        for (int i = 0; i < MAX_REQUESTS_PER_MINUTE; i++) {
            assertTrue(rateLimiter.tryAcquire(CLIENT_A), "bucket should be fully refilled after a window");
        }
    }

    private void exhaustLimit(String clientKey) {
        for (int i = 0; i < MAX_REQUESTS_PER_MINUTE; i++) {
            rateLimiter.tryAcquire(clientKey);
        }
    }

    private void advanceTimeBy(Duration duration) {
        fakeNanoTime.addAndGet(duration.toNanos());
    }
}
