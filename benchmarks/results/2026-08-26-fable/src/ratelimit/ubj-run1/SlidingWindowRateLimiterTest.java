import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SlidingWindowRateLimiterTest {

    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    /** Clock whose time the test advances explicitly. */
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        void advance(Duration duration) { now = now.plus(duration); }

        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }

    private final MutableClock clock = new MutableClock();
    private final SlidingWindowRateLimiter limiter =
            new SlidingWindowRateLimiter(LIMIT, WINDOW, clock);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire("participant_001"));
        }
    }

    @Test
    void rejectsRequestsOverTheLimit() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("participant_001");
        }
        assertFalse(limiter.tryAcquire("participant_001"));
    }

    @Test
    void allowsAgainOnceOldRequestsLeaveTheWindow() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("participant_001");
        }
        clock.advance(WINDOW.plusSeconds(1));
        assertTrue(limiter.tryAcquire("participant_001"));
    }

    @Test
    void limitsClientsIndependently() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("participant_001");
        }
        assertTrue(limiter.tryAcquire("participant_002"));
    }

    @Test
    void reportsSecondsUntilNextSlotWhenLimited() {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire("participant_001");
        }
        assertEquals(WINDOW.getSeconds(), limiter.secondsUntilNextSlot("participant_001"));
    }

    @Test
    void reportsZeroWaitWhenUnderTheLimit() {
        limiter.tryAcquire("participant_001");
        assertEquals(0, limiter.secondsUntilNextSlot("participant_001"));
    }

    @Test
    void idleSweepDoesNotDropActiveClients() {
        limiter.tryAcquire("participant_001");
        limiter.removeIdleClients();
        for (int i = 1; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire("participant_001"));
        }
        assertFalse(limiter.tryAcquire("participant_001"));
    }
}
