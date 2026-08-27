// TokenBucketRateLimiterTest.java
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBucketRateLimiterTest {

    private static final String CLIENT_A = "participant_001";
    private static final String CLIENT_B = "participant_002";
    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);

    @Test
    void allowsUpToCapacityRequestsImmediately() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(
                3, 3, ONE_MINUTE, new AdjustableTestClock(Instant.EPOCH));

        assertTrue(limiter.allowRequest(CLIENT_A));
        assertTrue(limiter.allowRequest(CLIENT_A));
        assertTrue(limiter.allowRequest(CLIENT_A));
    }

    @Test
    void rejectsRequestBeyondCapacityWithinSamePeriod() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(
                2, 2, ONE_MINUTE, new AdjustableTestClock(Instant.EPOCH));

        limiter.allowRequest(CLIENT_A);
        limiter.allowRequest(CLIENT_A);

        assertFalse(limiter.allowRequest(CLIENT_A));
    }

    @Test
    void refillsTokensAfterPeriodElapses() {
        AdjustableTestClock clock = new AdjustableTestClock(Instant.EPOCH);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1, 1, ONE_MINUTE, clock);

        assertTrue(limiter.allowRequest(CLIENT_A));
        assertFalse(limiter.allowRequest(CLIENT_A));

        clock.advanceBy(ONE_MINUTE);

        assertTrue(limiter.allowRequest(CLIENT_A));
    }

    @Test
    void tracksEachClientIndependently() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(
                1, 1, ONE_MINUTE, new AdjustableTestClock(Instant.EPOCH));

        assertTrue(limiter.allowRequest(CLIENT_A));
        assertFalse(limiter.allowRequest(CLIENT_A));
        assertTrue(limiter.allowRequest(CLIENT_B));
    }
}
