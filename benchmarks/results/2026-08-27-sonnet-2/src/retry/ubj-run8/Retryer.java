package com.example.retry;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Runs an operation, retrying it a fixed number of times with a fixed delay
 * between attempts whenever it throws an exception.
 */
public final class Retryer {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;
    private final Sleeper sleeper;

    public Retryer(int maxAttempts, Duration delayBetweenAttempts) {
        this(maxAttempts, delayBetweenAttempts, Retryer::sleepFor);
    }

    Retryer(int maxAttempts, Duration delayBetweenAttempts, Sleeper sleeper) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
        this.sleeper = sleeper;
    }

    /**
     * Runs {@code operation}, retrying on any exception until it succeeds or
     * {@code maxAttempts} is reached.
     *
     * @throws RetryExhaustedException if every attempt fails
     * @throws IllegalStateException   if interrupted while waiting to retry
     */
    public <T> T execute(Callable<T> operation) {
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
                waitBeforeNextAttempt(attempt);
            }
        }
        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private void waitBeforeNextAttempt(int attemptJustFailed) {
        if (attemptJustFailed >= maxAttempts) {
            return;
        }
        try {
            sleeper.sleep(delayBetweenAttempts);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrupted while waiting to retry", e);
        }
    }

    private static void sleepFor(Duration duration) throws InterruptedException {
        Thread.sleep(duration.toMillis());
    }
}
