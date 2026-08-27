package com.plg.ratelimit;

/**
 * Immutable token-bucket state. Tokens refill continuously between the
 * configured capacity and zero, proportional to elapsed time.
 */
final class TokenBucket {

    private final double availableTokens;
    private final long lastRefillNanos;

    private TokenBucket(double availableTokens, long lastRefillNanos) {
        this.availableTokens = availableTokens;
        this.lastRefillNanos = lastRefillNanos;
    }

    static TokenBucket full(int capacity, long nowNanos) {
        return new TokenBucket(capacity, nowNanos);
    }

    TokenBucket refill(long nowNanos, int capacity, long windowNanos) {
        long elapsedNanos = Math.max(0, nowNanos - lastRefillNanos);
        double refillRatePerNano = (double) capacity / windowNanos;
        double refilled = Math.min(capacity, availableTokens + elapsedNanos * refillRatePerNano);
        return new TokenBucket(refilled, nowNanos);
    }

    boolean hasTokenAvailable() {
        return availableTokens >= 1.0;
    }

    TokenBucket consumeOne() {
        return new TokenBucket(availableTokens - 1.0, lastRefillNanos);
    }
}
