package com.example.retry;

import java.time.Duration;

/**
 * Configuration for a retry run.
 *
 * @param maxAttempts total number of tries, including the first (must be >= 1)
 * @param delay       fixed wait time between attempts (must not be negative)
 */
public record RetryPolicy(int maxAttempts, Duration delay) {

    private static final int MIN_ATTEMPTS = 1;

    public RetryPolicy {
        if (maxAttempts < MIN_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be >= " + MIN_ATTEMPTS);
        }
        Objects.requireNonNull(delay, "delay must not be null");
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative");
        }
    }

    public static RetryPolicy of(int maxAttempts, Duration delay) {
        return new RetryPolicy(maxAttempts, delay);
    }
}
