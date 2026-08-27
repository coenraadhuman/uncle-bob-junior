// File: Retryer.java
package com.plg.retry;

import java.time.Duration;
import java.util.Objects;

/**
 * Runs an operation, retrying it a fixed number of times with a fixed
 * delay between attempts when it throws an exception.
 *
 * <p>Instances are immutable and safe to reuse across many calls to
 * {@link #run(RetryableOperation)}.
 */
public final class Retryer {

    private static final int MIN_ATTEMPTS = 1;

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;
    private final Sleeper sleeper;

    /**
     * @param maxAttempts           total number of attempts, must be at least 1
     * @param delayBetweenAttempts  wait time between attempts, must not be negative
     */
    public Retryer(int maxAttempts, Duration delayBetweenAttempts) {
        this(maxAttempts, delayBetweenAttempts, new ThreadSleeper());
    }

    Retryer(int maxAttempts, Duration delayBetweenAttempts, Sleeper sleeper) {
        this.delayBetweenAttempts = requireNonNegative(
                Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts must not be null"));
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper must not be null");
        this.maxAttempts = requireValidAttemptCount(maxAttempts);
    }

    /**
     * Runs {@code operation}, retrying on any {@link Exception} up to
     * {@code maxAttempts} times with {@code delayBetweenAttempts} between tries.
     *
     * @throws RetryExhaustedException     if every attempt fails
     * @throws RetryInterruptedException   if interrupted while waiting to retry
     */
    public <T> T run(RetryableOperation<T> operation) {
        Objects.requireNonNull(operation, "operation must not be null");

        Exception lastFailure = null;
        for (int attemptNumber = MIN_ATTEMPTS; attemptNumber <= maxAttempts; attemptNumber++) {
            AttemptResult<T> result = attempt(operation);
            if (result.succeeded()) {
                return result.value();
            }
            lastFailure = result.failure();
            if (hasAttemptsRemaining(attemptNumber)) {
                sleeper.sleep(delayBetweenAttempts);
            }
        }
        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private <T> AttemptResult<T> attempt(RetryableOperation<T> operation) {
        try {
            return AttemptResult.success(operation.execute());
        } catch (Exception failure) {
            return AttemptResult.failure(failure);
        }
    }

    private boolean hasAttemptsRemaining(int attemptNumber) {
        return attemptNumber < maxAttempts;
    }

    private static int requireValidAttemptCount(int maxAttempts) {
        if (maxAttempts < MIN_ATTEMPTS) {
            throw new IllegalArgumentException(
                    "maxAttempts must be at least " + MIN_ATTEMPTS + " but was " + maxAttempts);
        }
        return maxAttempts;
    }

    private static Duration requireNonNegative(Duration delay) {
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative but was " + delay);
        }
        return delay;
    }

    /** Outcome of a single attempt: exactly one of value/failure is meaningful. */
    private static final class AttemptResult<T> {
        private final T value;
        private final Exception failure;
        private final boolean succeeded;

        private AttemptResult(T value, Exception failure, boolean succeeded) {
            this.value = value;
            this.failure = failure;
            this.succeeded = succeeded;
        }

        static <T> AttemptResult<T> success(T value) {
            return new AttemptResult<>(value, null, true);
        }

        static <T> AttemptResult<T> failure(Exception failure) {
            return new AttemptResult<>(null, failure, false);
        }

        boolean succeeded() {
            return succeeded;
        }

        T value() {
            return value;
        }

        Exception failure() {
            return failure;
        }
    }

    /** Seam for injecting a fake wait in tests instead of really sleeping. */
    interface Sleeper {
        void sleep(Duration duration);
    }

    private static final class ThreadSleeper implements Sleeper {
        @Override
        public void sleep(Duration duration) {
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new RetryInterruptedException(interrupted);
            }
        }
    }
}
