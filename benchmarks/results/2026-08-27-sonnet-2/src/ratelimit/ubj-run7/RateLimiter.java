package com.plg.ratelimit;

@FunctionalInterface
public interface RateLimiter {
    boolean tryAcquire(String clientId);
}
