package com.plg.ratelimit;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/**
 * Per-client token-bucket rate limiter.
 *
 * <p>Thread-safe: each client's bucket is updated via a lock-free
 * compare-and-swap loop, so concurrent requests from the same client
 * never over-consume tokens.
 */
public final class RateLimiter {

    private final int maxRequestsPerWindow;
    private final long windowNanos;
    private final LongSupplier nanoClock;
    private final ConcurrentHashMap<String, AtomicReference<TokenBucket>> bucketsByClient =
            new ConcurrentHashMap<>();

    public RateLimiter(int maxRequestsPerWindow, Duration window) {
        this(maxRequestsPerWindow, window, System::nanoTime);
    }

    RateLimiter(int maxRequestsPerWindow, Duration window, LongSupplier nanoClock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowNanos = window.toNanos();
        this.nanoClock = nanoClock;
    }

    public boolean tryAcquire(String clientKey) {
        long now = nanoClock.getAsLong();
        AtomicReference<TokenBucket> bucketRef = bucketsByClient.computeIfAbsent(
                clientKey, key -> new AtomicReference<>(TokenBucket.full(maxRequestsPerWindow, now)));

        while (true) {
            TokenBucket current = bucketRef.get();
            TokenBucket refilled = current.refill(now, maxRequestsPerWindow, windowNanos);

            if (!refilled.hasTokenAvailable()) {
                bucketRef.compareAndSet(current, refilled);
                return false;
            }

            if (bucketRef.compareAndSet(current, refilled.consumeOne())) {
                return true;
            }
        }
    }
}
