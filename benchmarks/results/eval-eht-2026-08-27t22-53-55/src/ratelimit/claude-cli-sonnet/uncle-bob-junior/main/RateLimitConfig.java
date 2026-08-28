// RateLimitConfig.java
package com.example.ratelimit;

import java.time.Duration;

public record RateLimitConfig(int maxRequests, Duration window) {

    public RateLimitConfig {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("maxRequests must be positive");
        }
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window must be positive");
        }
    }
}
