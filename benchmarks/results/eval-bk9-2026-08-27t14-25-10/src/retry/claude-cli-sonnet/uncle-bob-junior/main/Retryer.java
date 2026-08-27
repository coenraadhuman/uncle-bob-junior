package com.postcodeloterij.retry;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Runs an operation, retrying it a fixed number of times with a fixed
 * delay between attempts whenever it throws an exception.
 */
public final class Retryer {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    public Retryer(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    /**
     * Executes {@code operation}, retrying on any exception up to
     * {@code maxAttempts} times in total.
     *
     * @throws RetryExhaustedException if every attempt failed
     * @throws RetryInterruptedException if interrupted while waiting to retry
     */
    public <T> T run(Callable<T> operation) {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
                if (attempt < maxAttempts) {
                    waitBeforeNextAttempt();
                }
            }
        }

        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private void waitBeforeNextAttempt() {
        try {
            Thread.sleep(delayBetweenAttempts.toMillis());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(interrupted);
        }
    }
}
