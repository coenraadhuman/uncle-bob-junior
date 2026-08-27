// Retryer.java
package com.plg.retry;

import java.time.Duration;
import java.util.Objects;

public final class Retryer {

    private Retryer() {
    }

    public static <T> T run(Operation<T> operation, RetryPolicy policy) {
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(policy, "policy");

        Exception lastFailure = null;
        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            try {
                return operation.execute();
            } catch (Exception failure) {
                lastFailure = failure;
                if (attempt < policy.maxAttempts()) {
                    waitBeforeRetry(policy.delayBetweenAttempts());
                }
            }
        }
        throw new RetryExhaustedException(policy.maxAttempts(), lastFailure);
    }

    private static void waitBeforeRetry(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(interrupted);
        }
    }
}
