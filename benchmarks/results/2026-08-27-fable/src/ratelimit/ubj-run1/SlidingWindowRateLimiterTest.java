package ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SlidingWindowRateLimiterTest {

    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final String CLIENT = "203.0.113.7";

    private final SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(LIMIT, WINDOW);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire(CLIENT, START.plusSeconds(i)).allowed());
        }
    }

    @Test
    void rejectsTheRequestOverTheLimitAndSaysWhenToRetry() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire(CLIENT, START);
        }
        SlidingWindowRateLimiter.Decision decision =
                limiter.tryAcquire(CLIENT, START.plusSeconds(10));

        assertFalse(decision.allowed());
        assertEquals(50, decision.retryAfterSeconds()); // oldest request leaves the window at +60s
    }

    @Test
    void allowsAgainOnceTheWindowHasPassed() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire(CLIENT, START);
        }
        assertTrue(limiter.tryAcquire(CLIENT, START.plus(WINDOW)).allowed());
    }

    @Test
    void windowSlidesInsteadOfResetting() {
        limiter.tryAcquire(CLIENT, START);
        limiter.tryAcquire(CLIENT, START.plusSeconds(30));
        limiter.tryAcquire(CLIENT, START.plusSeconds(30));

        assertFalse(limiter.tryAcquire(CLIENT, START.plusSeconds(59)).allowed());
        // at +60s only the first request has expired, freeing exactly one slot
        assertTrue(limiter.tryAcquire(CLIENT, START.plusSeconds(60)).allowed());
        assertFalse(limiter.tryAcquire(CLIENT, START.plusSeconds(60)).allowed());
    }

    @Test
    void tracksEachClientIndependently() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire(CLIENT, START);
        }
        assertTrue(limiter.tryAcquire("198.51.100.9", START).allowed());
    }

    @Test
    void rejectsNonPositiveLimit() {
        assertThrows(IllegalArgumentException.class, () -> new SlidingWindowRateLimiter(0, WINDOW));
    }
}
