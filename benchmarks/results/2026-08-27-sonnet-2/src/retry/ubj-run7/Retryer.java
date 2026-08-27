package com.example.retry;

import java.time.Duration;
import java.util.Objects;

/**
 * Runs an operation, retrying on failure up to a configured number of
 * attempts with a fixed delay between them.
 */
public final class Retryer {

    private Retryer() {
    }

    public static <T> T execute(RetryableOperation<T> operation, RetryPolicy policy) throws InterruptedException {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(policy, "policy must not be null");

        Exception lastFailure = null;
        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            try {
                return operation.execute();
            } catch (Exception failure) {
                lastFailure = failure;
                if (hasAttemptsRemaining(attempt, policy)) {
                    sleep(policy.delay());
                }
            }
        }
        throw new RetryExhaustedException(policy.maxAttempts(), lastFailure);
    }

    private static boolean hasAttemptsRemaining(int attemptJustMade, RetryPolicy policy) {
        return attemptJustMade < policy.maxAttempts();
    }

    private static void sleep(Duration delay) throws InterruptedException {
        Thread.sleep(delay.toMillis());
    }
}
