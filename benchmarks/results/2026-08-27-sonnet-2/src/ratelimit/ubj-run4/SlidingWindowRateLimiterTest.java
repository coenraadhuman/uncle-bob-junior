// SlidingWindowRateLimiterTest.java
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowRateLimiterTest {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private MutableClock clock;
    private SlidingWindowRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        rateLimiter = new SlidingWindowRateLimiter(MAX_REQUESTS, WINDOW, clock);
    }

    @AfterEach
    void tearDown() {
        rateLimiter.close();
    }

    @Test
    void allowsRequestsUpToTheLimit() {
        assertTrue(rateLimiter.tryAcquire("client-a"));
        assertTrue(rateLimiter.tryAcquire("client-a"));
        assertTrue(rateLimiter.tryAcquire("client-a"));
    }

    @Test
    void blocksRequestsBeyondTheLimit() {
        acquireMaxRequests("client-a");
        assertFalse(rateLimiter.tryAcquire("client-a"));
    }

    @Test
    void allowsRequestsAgainAfterWindowElapses() {
        acquireMaxRequests("client-a");
        clock.advanceBy(WINDOW.plusSeconds(1));
        assertTrue(rateLimiter.tryAcquire("client-a"));
    }

    @Test
    void tracksSeparateClientsIndependently() {
        acquireMaxRequests("client-a");
        assertTrue(rateLimiter.tryAcquire("client-b"));
    }

    private void acquireMaxRequests(String clientId) {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.tryAcquire(clientId);
        }
    }

    private static final class MutableClock extends Clock {
        private Instant currentInstant;

        MutableClock(Instant initialInstant) {
            this.currentInstant = initialInstant;
        }

        void advanceBy(Duration duration) {
            currentInstant = currentInstant.plus(duration);
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException();
        }
    }
}
