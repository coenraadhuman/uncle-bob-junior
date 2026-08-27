package ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowRateLimiterTest {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    @Test
    void allowsRequestsUpToTheLimitWithinAWindow() {
        FixedWindowRateLimiter limiter = newLimiterWithFakeClock(new AtomicLong(0));

        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(limiter.tryAcquire("client-a"));
        }
    }

    @Test
    void blocksRequestsBeyondTheLimitWithinAWindow() {
        FixedWindowRateLimiter limiter = newLimiterWithFakeClock(new AtomicLong(0));

        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.tryAcquire("client-a");
        }

        assertFalse(limiter.tryAcquire("client-a"));
    }

    @Test
    void resetsTheLimitOnceTheWindowElapses() {
        AtomicLong fakeNow = new AtomicLong(0);
        FixedWindowRateLimiter limiter = newLimiterWithFakeClock(fakeNow);

        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.tryAcquire("client-a");
        }
        assertFalse(limiter.tryAcquire("client-a"));

        fakeNow.set(WINDOW.toMillis() + 1);

        assertTrue(limiter.tryAcquire("client-a"));
    }

    @Test
    void tracksEachClientIndependently() {
        FixedWindowRateLimiter limiter = newLimiterWithFakeClock(new AtomicLong(0));

        for (int i = 0; i < MAX_REQUESTS; i++) {
            limiter.tryAcquire("client-a");
        }

        assertTrue(limiter.tryAcquire("client-b"));
    }

    private FixedWindowRateLimiter newLimiterWithFakeClock(AtomicLong fakeNow) {
        return new FixedWindowRateLimiter(MAX_REQUESTS, WINDOW, fakeNow::get, false);
    }
}
