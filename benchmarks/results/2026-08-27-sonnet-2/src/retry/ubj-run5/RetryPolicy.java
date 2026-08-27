// RetryPolicy.java
package com.plg.retry;

import java.time.Duration;
import java.util.Objects;

public final class RetryPolicy {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    private RetryPolicy(int maxAttempts, Duration delayBetweenAttempts) {
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    public static RetryPolicy of(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, was " + maxAttempts);
        }
        Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative");
        }
        return new RetryPolicy(maxAttempts, delayBetweenAttempts);
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public Duration delayBetweenAttempts() {
        return delayBetweenAttempts;
    }
}
