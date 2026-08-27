// File: TokenBucketRateLimiter.java
package com.postcodeloterij.ratelimit;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.LongSupplier;

/**
 * Per-client request limiter using a token bucket: each client key gets
 * {@code maxRequestsPerWindow} tokens that refill continuously over
 * {@code window}, so bursts are smoothed instead of reset at a fixed boundary.
 */
public final class TokenBucketRateLimiter {

    private final int maxRequestsPerWindow;
    private final long windowNanos;
    private final LongSupplier nanoClock;
    private final ConcurrentMap<String, TokenBucket> bucketsByClientKey = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(int maxRequestsPerWindow, Duration window) {
        this(maxRequestsPerWindow, window, System::nanoTime);
    }

    TokenBucketRateLimiter(int maxRequestsPerWindow, Duration window, LongSupplier nanoClock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowNanos = window.toNanos();
        this.nanoClock = nanoClock;
    }

    /** Returns true if the client may proceed, false if it must be rejected. */
    public boolean tryAcquire(String clientKey) {
        TokenBucket bucket = bucketsByClientKey.computeIfAbsent(
                clientKey, key -> new TokenBucket(maxRequestsPerWindow, windowNanos, nanoClock));
        return bucket.tryConsume();
    }

    private static final class TokenBucket {
        private static final double MIN_TOKENS_TO_CONSUME = 1.0;

        private final double capacity;
        private final double refillTokensPerNano;
        private final LongSupplier nanoClock;
        private double availableTokens;
        private long lastRefillNanos;

        TokenBucket(int capacity, long windowNanos, LongSupplier nanoClock) {
            this.capacity = capacity;
            this.refillTokensPerNano = (double) capacity / windowNanos;
            this.nanoClock = nanoClock;
            this.availableTokens = capacity;
            this.lastRefillNanos = nanoClock.getAsLong();
        }

        synchronized boolean tryConsume() {
            refill();
            if (availableTokens < MIN_TOKENS_TO_CONSUME) {
                return false;
            }
            availableTokens -= MIN_TOKENS_TO_CONSUME;
            return true;
        }

        private void refill() {
            long now = nanoClock.getAsLong();
            long elapsedNanos = now - lastRefillNanos;
            if (elapsedNanos <= 0) {
                return;
            }
            availableTokens = Math.min(capacity, availableTokens + elapsedNanos * refillTokensPerNano);
            lastRefillNanos = now;
        }
    }
}
