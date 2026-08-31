import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SlidingWindowRateLimiterTest {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT_A = "10.0.0.1";
    private static final String CLIENT_B = "10.0.0.2";

    /** Test clock that only moves when the test advances it. */
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-08-28T12:00:00Z");

        void advance(Duration amount) { now = now.plus(amount); }

        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }

    private final MutableClock clock = new MutableClock();
    private final SlidingWindowRateLimiter limiter =
            new SlidingWindowRateLimiter(MAX_REQUESTS, WINDOW, clock);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int request = 0; request < MAX_REQUESTS; request++) {
            assertTrue(limiter.tryAcquire(CLIENT_A));
        }
    }

    @Test
    void deniesTheRequestOverTheLimit() {
        for (int request = 0; request < MAX_REQUESTS; request++) {
            limiter.tryAcquire(CLIENT_A);
        }
        assertFalse(limiter.tryAcquire(CLIENT_A));
    }

    @Test
    void allowsAgainOnceTheWindowHasPassed() {
        for (int request = 0; request < MAX_REQUESTS; request++) {
            limiter.tryAcquire(CLIENT_A);
        }
        clock.advance(WINDOW.plusSeconds(1));
        assertTrue(limiter.tryAcquire(CLIENT_A));
    }

    @Test
    void limitsClientsIndependently() {
        for (int request = 0; request < MAX_REQUESTS; request++) {
            limiter.tryAcquire(CLIENT_A);
        }
        assertFalse(limiter.tryAcquire(CLIENT_A));
        assertTrue(limiter.tryAcquire(CLIENT_B));
    }

    @Test
    void slidesRatherThanResettingInFixedBlocks() {
        limiter.tryAcquire(CLIENT_A);
        clock.advance(Duration.ofSeconds(30));
        limiter.tryAcquire(CLIENT_A);
        limiter.tryAcquire(CLIENT_A);
        clock.advance(Duration.ofSeconds(31)); // first request has expired, later two have not
        assertTrue(limiter.tryAcquire(CLIENT_A));
        assertFalse(limiter.tryAcquire(CLIENT_A));
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> new SlidingWindowRateLimiter(0, WINDOW, clock));
        assertThrows(IllegalArgumentException.class,
                () -> new SlidingWindowRateLimiter(MAX_REQUESTS, Duration.ZERO, clock));
    }

    @Test
    void reportsWindowLengthInSeconds() {
        assertEquals(WINDOW.toSeconds(), limiter.windowSeconds());
    }
}
