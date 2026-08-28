package com.example.retry;

import java.time.Duration;
import java.util.Objects;

/**
 * Fixed number of attempts, each separated by a fixed delay.
 *
 * @param maxAttempts total number of attempts, including the first (must be >= 1)
 * @param delay       wait time between attempts (must not be negative)
 */
public record RetryPolicy(int maxAttempts, Duration delay) {

    public RetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        Objects.requireNonNull(delay, "delay");
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative");
        }
    }
}
