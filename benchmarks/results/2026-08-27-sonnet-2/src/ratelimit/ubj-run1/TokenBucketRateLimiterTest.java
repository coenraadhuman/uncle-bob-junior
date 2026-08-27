// File: TokenBucketRateLimiterTest.java
package com.postcodeloterij.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBucketRateLimiterTest {

    private static final int MAX_REQUESTS_PER_MINUTE = 3;
    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);
    private static final String CLIENT_A = "192.0.2.1";
    private static final String CLIENT_B = "192.0.2.2";

    private final AtomicLong fakeNanos = new AtomicLong(0);
    private final TokenBucketRateLimiter rateLimiter =
            new TokenBucketRateLimiter(MAX_REQUESTS_PER_MINUTE, ONE_MINUTE, fakeNanos::get);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int i = 0; i < MAX_REQUESTS_PER_MINUTE; i++) {
            assertTrue(rateLimiter.tryAcquire(CLIENT_A), "request " + i + " should be allowed");
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimit() {
        exhaustLimitFor(CLIENT_A);

        assertFalse(rateLimiter.tryAcquire(CLIENT_A));
    }

    @Test
    void refillsTokensAfterFullWindowElapses() {
        exhaustLimitFor(CLIENT_A);

        fakeNanos.addAndGet(ONE_MINUTE.toNanos());

        assertTrue(rateLimiter.tryAcquire(CLIENT_A));
    }

    @Test
    void partiallyRefillsTokensProportionallyToElapsedTime() {
        exhaustLimitFor(CLIENT_A);

        fakeNanos.addAndGet(ONE_MINUTE.toNanos() / MAX_REQUESTS_PER_MINUTE);

        assertTrue(rateLimiter.tryAcquire(CLIENT_A), "one token should have refilled");
        assertFalse(rateLimiter.tryAcquire(CLIENT_A), "only one token should have refilled");
    }

    @Test
    void tracksEachClientIndependently() {
        exhaustLimitFor(CLIENT_A);

        assertTrue(rateLimiter.tryAcquire(CLIENT_B), "a different client should have its own budget");
    }

    @Test
    void rejectsNonPositiveLimits() {
        assertThrows(IllegalArgumentException.class,
                () -> new TokenBucketRateLimiter(0, ONE_MINUTE));
    }

    private void exhaustLimitFor(String clientKey) {
        for (int i = 0; i < MAX_REQUESTS_PER_MINUTE; i++) {
            assertTrue(rateLimiter.tryAcquire(clientKey));
        }
    }
}
