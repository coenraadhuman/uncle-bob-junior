// FixedWindowRateLimiterTest.java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowRateLimiterTest {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    private MutableClock clock;
    private FixedWindowRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        rateLimiter = new FixedWindowRateLimiter(MAX_REQUESTS, WINDOW, clock);
    }

    @Test
    void allowsRequestsUpToLimit() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.tryAcquire("client-1"));
        }
    }

    @Test
    void rejectsRequestBeyondLimit() {
        exhaustLimitFor("client-1");
        assertFalse(rateLimiter.tryAcquire("client-1"));
    }

    @Test
    void resetsAfterWindowElapses() {
        exhaustLimitFor("client-1");
        clock.advanceBy(WINDOW);
        assertTrue(rateLimiter.tryAcquire("client-1"));
    }

    @Test
    void tracksClientsIndependently() {
        exhaustLimitFor("client-1");
        assertTrue(rateLimiter.tryAcquire("client-2"));
    }

    private void exhaustLimitFor(String clientId) {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.tryAcquire(clientId);
        }
    }
}
