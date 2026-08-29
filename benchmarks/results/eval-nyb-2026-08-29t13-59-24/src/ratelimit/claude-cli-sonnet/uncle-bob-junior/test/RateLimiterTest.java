package ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    private static final int MAX_REQUESTS_PER_WINDOW = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    @Test
    void allowsUpToTheLimitWithinAWindow() {
        long[] now = {0L};
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW, () -> now[0]);

        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            assertTrue(limiter.tryAcquire("client-1"));
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimitWithinAWindow() {
        long[] now = {0L};
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW, () -> now[0]);

        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            limiter.tryAcquire("client-1");
        }

        assertFalse(limiter.tryAcquire("client-1"));
    }

    @Test
    void resetsTheCountAfterTheWindowElapses() {
        long[] now = {0L};
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW, () -> now[0]);
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            limiter.tryAcquire("client-1");
        }
        assertFalse(limiter.tryAcquire("client-1"));

        now[0] += WINDOW.toMillis();

        assertTrue(limiter.tryAcquire("client-1"));
    }

    @Test
    void tracksEachClientIndependently() {
        long[] now = {0L};
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW, () -> now[0]);
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            limiter.tryAcquire("client-1");
        }

        assertTrue(limiter.tryAcquire("client-2"));
    }
}
