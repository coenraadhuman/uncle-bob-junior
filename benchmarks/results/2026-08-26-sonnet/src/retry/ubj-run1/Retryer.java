// Retryer.java
package com.plg.retry;

public final class Retryer {

    public <T> T run(RetryableOperation<T> operation, RetryConfig config) {
        Throwable lastFailure = null;

        for (int attempt = 1; attempt <= config.getMaxAttempts(); attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
            }
            delayBeforeNextAttempt(attempt, config);
        }

        throw new RetryExhaustedException(config.getMaxAttempts(), lastFailure);
    }

    private void delayBeforeNextAttempt(int attempt, RetryConfig config) {
        boolean hasMoreAttempts = attempt < config.getMaxAttempts();
        if (!hasMoreAttempts) {
            return;
        }
        sleep(config.getDelayBetweenAttempts());
    }

    private void sleep(java.time.Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(interruptedException);
        }
    }
}
