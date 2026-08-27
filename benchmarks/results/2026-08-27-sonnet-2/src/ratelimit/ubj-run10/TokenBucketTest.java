import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TokenBucketTest {

    private static final class MutableFakeClock extends Clock {
        private long currentMillis;

        MutableFakeClock(long initialMillis) {
            this.currentMillis = initialMillis;
        }

        void advanceMillis(long millis) {
            currentMillis += millis;
        }

        @Override
        public long millis() {
            return currentMillis;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(currentMillis);
        }
    }

    @Test
    void allowsUpToCapacityRequestsImmediately() {
        TokenBucket bucket = new TokenBucket(3, Duration.ofMinutes(1), new MutableFakeClock(0));

        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());
    }

    @Test
    void rejectsRequestBeyondCapacityWithinWindow() {
        TokenBucket bucket = new TokenBucket(2, Duration.ofMinutes(1), new MutableFakeClock(0));

        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());
    }

    @Test
    void refillsTokensAfterWindowElapses() {
        MutableFakeClock clock = new MutableFakeClock(0);
        TokenBucket bucket = new TokenBucket(1, Duration.ofMinutes(1), clock);

        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());

        clock.advanceMillis(Duration.ofMinutes(1).toMillis());

        assertTrue(bucket.tryConsume());
    }

    @Test
    void partialElapsedTimeGrantsOnlyPartialTokens() {
        MutableFakeClock clock = new MutableFakeClock(0);
        TokenBucket bucket = new TokenBucket(2, Duration.ofMinutes(1), clock);
        bucket.tryConsume();
        bucket.tryConsume();

        clock.advanceMillis(Duration.ofSeconds(29).toMillis());

        assertFalse(bucket.tryConsume());
    }
}
