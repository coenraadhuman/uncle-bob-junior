package ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowRateLimiterTest {

    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT = "client-a";
    private static final String OTHER_CLIENT = "client-b";

    private final MutableClock clock = new MutableClock(Instant.parse("2026-08-27T10:00:00Z"));
    private final SlidingWindowRateLimiter limiter =
            new SlidingWindowRateLimiter(LIMIT, WINDOW, clock);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int request = 0; request < LIMIT; request++) {
            assertTrue(limiter.check(CLIENT).allowed());
        }
    }

    @Test
    void rejectsTheRequestThatExceedsTheLimit() {
        exhaustLimit(CLIENT);
        assertFalse(limiter.check(CLIENT).allowed());
    }

    @Test
    void countsEachClientSeparately() {
        exhaustLimit(CLIENT);
        assertTrue(limiter.check(OTHER_CLIENT).allowed());
    }

    @Test
    void freesOnlyTheSlotsThatHaveLeftTheWindow() {
        limiter.check(CLIENT);
        clock.advance(Duration.ofSeconds(30));
        limiter.check(CLIENT);
        limiter.check(CLIENT);
        clock.advance(Duration.ofSeconds(30));

        assertTrue(limiter.check(CLIENT).allowed(), "first request expired, slot is free");
        assertFalse(limiter.check(CLIENT).allowed(), "the two mid-window requests still count");
    }

    @Test
    void reportsHowLongUntilASlotFreesUp() {
        exhaustLimit(CLIENT);
        clock.advance(Duration.ofSeconds(15));

        RateLimitDecision decision = limiter.check(CLIENT);

        assertFalse(decision.allowed());
        assertEquals(Duration.ofSeconds(45), decision.retryAfter());
    }

    @Test
    void allowedDecisionNeedsNoWait() {
        assertEquals(Duration.ZERO, limiter.check(CLIENT).retryAfter());
    }

    @Test
    void purgeDropsClientsWhoseRequestsHaveAllExpired() {
        limiter.check(CLIENT);
        clock.advance(WINDOW.plusSeconds(1));
        limiter.check(OTHER_CLIENT);

        limiter.purgeIdleClients();

        assertEquals(1, limiter.trackedClientCount());
    }

    @Test
    void purgeKeepsRequestsThatAreStillInsideTheWindow() {
        exhaustLimit(CLIENT);
        clock.advance(Duration.ofSeconds(30));

        limiter.purgeIdleClients();

        assertFalse(limiter.check(CLIENT).allowed());
    }

    @Test
    void rejectsALimitBelowOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new SlidingWindowRateLimiter(0, WINDOW, clock));
    }

    @Test
    void rejectsANonPositiveWindow() {
        assertThrows(IllegalArgumentException.class,
                () -> new SlidingWindowRateLimiter(LIMIT, Duration.ZERO, clock));
    }

    private void exhaustLimit(String clientId) {
        for (int request = 0; request < LIMIT; request++) {
            limiter.check(clientId);
        }
    }
}
