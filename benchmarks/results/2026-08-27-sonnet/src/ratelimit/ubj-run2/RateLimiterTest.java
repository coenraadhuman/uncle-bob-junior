import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    @Test
    void allowsRequestsUpToTheLimit() {
        RateLimiter limiter = newLimiterAt(Instant.EPOCH);

        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire("client-a"), "request " + i + " should be allowed");
        }
    }

    @Test
    void blocksRequestsBeyondTheLimitWithinTheSameWindow() {
        RateLimiter limiter = newLimiterAt(Instant.EPOCH);
        exhaustLimit(limiter, "client-a");

        assertFalse(limiter.tryAcquire("client-a"));
    }

    @Test
    void resetsTheLimitOnceTheWindowElapses() {
        MutableClock clock = new MutableClock(Instant.EPOCH);
        RateLimiter limiter = new RateLimiter(LIMIT, WINDOW, clock);
        exhaustLimit(limiter, "client-a");

        clock.advance(WINDOW.plusSeconds(1));

        assertTrue(limiter.tryAcquire("client-a"));
    }

    @Test
    void tracksEachClientIndependently() {
        RateLimiter limiter = newLimiterAt(Instant.EPOCH);
        exhaustLimit(limiter, "client-a");

        assertTrue(limiter.tryAcquire("client-b"));
    }

    private RateLimiter newLimiterAt(Instant now) {
        return new RateLimiter(LIMIT, WINDOW, new MutableClock(now));
    }

    private void exhaustLimit(RateLimiter limiter, String clientId) {
        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire(clientId);
        }
    }

    /** Test double: a {@link Clock} whose current instant can be advanced manually. */
    private static final class MutableClock extends Clock {
        private final AtomicLong epochMillis;

        MutableClock(Instant start) {
            this.epochMillis = new AtomicLong(start.toEpochMilli());
        }

        void advance(Duration duration) {
            epochMillis.addAndGet(duration.toMillis());
        }

        @Override
        public long millis() {
            return epochMillis.get();
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis());
        }
    }
}
