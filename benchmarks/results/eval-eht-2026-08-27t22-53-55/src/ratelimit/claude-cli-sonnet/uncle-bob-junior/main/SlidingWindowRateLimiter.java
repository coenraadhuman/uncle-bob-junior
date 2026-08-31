// SlidingWindowRateLimiter.java
package com.example.ratelimit;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SlidingWindowRateLimiter implements RateLimiter {

    private final RateLimitConfig config;
    private final Map<String, Deque<Instant>> requestTimestampsByClient = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(RateLimitConfig config) {
        this.config = config;
    }

    @Override
    public boolean tryAcquire(String clientId, Instant now) {
        Deque<Instant> timestamps = requestTimestampsByClient.computeIfAbsent(clientId, id -> new ArrayDeque<>());
        synchronized (timestamps) {
            evictExpired(timestamps, now);
            if (timestamps.size() >= config.maxRequests()) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    private void evictExpired(Deque<Instant> timestamps, Instant now) {
        Instant windowStart = now.minus(config.window());
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
            timestamps.pollFirst();
        }
    }
}
