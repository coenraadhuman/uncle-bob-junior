package com.plg.retry;

import java.time.Duration;
import java.util.Objects;

public final class RetryPolicy {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    public RetryPolicy(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, was " + maxAttempts);
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
    }

    int maxAttempts() {
        return maxAttempts;
    }

    Duration delayBetweenAttempts() {
        return delayBetweenAttempts;
    }
}
