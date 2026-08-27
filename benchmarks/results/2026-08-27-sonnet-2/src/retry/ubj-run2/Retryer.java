package com.plg.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Runs an operation, retrying it with a fixed delay between attempts when it
 * throws an exception, up to the number of attempts in the {@link RetryPolicy}.
 */
public final class Retryer {

    private final RetryPolicy policy;
    private final Sleeper sleeper;

    public Retryer(RetryPolicy policy) {
        this(policy, Sleeper.realTime());
    }

    public Retryer(RetryPolicy policy, Sleeper sleeper) {
        this.policy = Objects.requireNonNull(policy, "policy must not be null");
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper must not be null");
    }

    public <T> T run(Callable<T> operation) {
        Objects.requireNonNull(operation, "operation must not be null");
        Outcome<T> lastOutcome = null;
        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            lastOutcome = attempt(operation);
            if (lastOutcome.succeeded()) {
                return lastOutcome.value();
            }
            waitBeforeNextAttemptIfAny(attempt);
        }
        throw new RetryExhaustedException(policy.maxAttempts(), lastOutcome.failure());
    }

    private <T> Outcome<T> attempt(Callable<T> operation) {
        try {
            return Outcome.success(operation.call());
        } catch (Exception e) {
            return Outcome.failure(e);
        }
    }

    private void waitBeforeNextAttemptIfAny(int attemptJustMade) {
        if (attemptJustMade >= policy.maxAttempts()) {
            return;
        }
        sleepFor(policy.delayBetweenAttempts());
    }

    private void sleepFor(Duration delay) {
        try {
            sleeper.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(e);
        }
    }

    private static final class Outcome<T> {
        private final T value;
        private final Exception failure;

        private Outcome(T value, Exception failure) {
            this.value = value;
            this.failure = failure;
        }

        static <T> Outcome<T> success(T value) {
            return new Outcome<>(value, null);
        }

        static <T> Outcome<T> failure(Exception failure) {
            return new Outcome<>(null, failure);
        }

        boolean succeeded() {
            return failure == null;
        }

        T value() {
            return value;
        }

        Exception failure() {
            return failure;
        }
    }
}
