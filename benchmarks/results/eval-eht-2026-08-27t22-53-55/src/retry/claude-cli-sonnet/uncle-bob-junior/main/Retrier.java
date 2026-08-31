package com.plg.retry;

import java.util.Objects;
import java.util.concurrent.Callable;

public final class Retrier {

    private final RetryPolicy policy;
    private final Sleeper sleeper;

    public Retrier(RetryPolicy policy) {
        this(policy, Sleeper.SYSTEM);
    }

    Retrier(RetryPolicy policy, Sleeper sleeper) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    public <T> T run(Callable<T> operation) throws InterruptedException {
        Throwable lastFailure = null;

        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
                waitBeforeNextAttempt(attempt);
            }
        }

        throw new RetryExhaustedException(policy.maxAttempts(), lastFailure);
    }

    private void waitBeforeNextAttempt(int attemptJustFailed) throws InterruptedException {
        boolean moreAttemptsRemain = attemptJustFailed < policy.maxAttempts();
        if (moreAttemptsRemain) {
            sleeper.sleep(policy.delayBetweenAttempts());
        }
    }
}
