// File: Retryer.java
package com.example.retry;

import java.time.Duration;
import java.util.Objects;

public final class Retryer {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    public Retryer(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
    }

    public <T> T execute(RetryableOperation<T> operation) throws RetryExhaustedException {
        Objects.requireNonNull(operation, "operation");
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
                if (attempt < maxAttempts) {
                    sleepBetweenAttempts();
                }
            }
        }

        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private void sleepBetweenAttempts() {
        try {
            Thread.sleep(delayBetweenAttempts.toMillis());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry delay was interrupted", interrupted);
        }
    }
}
