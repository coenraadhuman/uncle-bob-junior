package com.example.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Runs an operation, retrying on any {@link Exception} up to a fixed number
 * of attempts, waiting a fixed delay between attempts.
 */
public final class Retryer {

    private final Sleeper sleeper;

    public Retryer() {
        this(delay -> Thread.sleep(delay.toMillis()));
    }

    public Retryer(Sleeper sleeper) {
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    /**
     * @throws RetryExhaustedException if every attempt fails
     * @throws RetryInterruptedException if the wait between attempts is interrupted
     */
    public <T> T execute(Callable<T> operation, RetryPolicy policy) {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(policy, "policy");

        Exception lastFailure = null;
        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            if (attempt > 1) {
                waitBeforeRetry(policy.delay());
            }
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
            }
        }
        throw new RetryExhaustedException(policy.maxAttempts(), lastFailure);
    }

    private void waitBeforeRetry(Duration delay) {
        try {
            sleeper.sleep(delay);
        } catch (InterruptedException interruption) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(interruption);
        }
    }
}
