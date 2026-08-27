// RetryConfig.java
package com.plg.retry;

import java.time.Duration;
import java.util.Objects;

public final class RetryConfig {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    public RetryConfig(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration getDelayBetweenAttempts() {
        return delayBetweenAttempts;
    }
}
