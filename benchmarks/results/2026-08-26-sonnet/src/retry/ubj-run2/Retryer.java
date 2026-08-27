package com.plg.retry;

import java.time.Duration;

/**
 * Runs a {@link RetryableTask}, retrying on failure up to a fixed number of
 * attempts with a fixed delay between them.
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
     * Executes {@code task}, retrying on any {@link Exception} until it
     * succeeds or {@code maxAttempts} is reached.
     *
     * @throws RetryExhaustedException if every attempt failed
     */
    public <T> T run(RetryableTask<T> task) {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return task.execute();
            } catch (Exception failure) {
                lastFailure = failure;
                if (isLastAttempt(attempt)) {
                    break;
                }
                waitBeforeNextAttempt();
            }
        }

        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private boolean isLastAttempt(int attempt) {
        return attempt == maxAttempts;
    }

    private void waitBeforeNextAttempt() {
        try {
            Thread.sleep(delayBetweenAttempts.toMillis());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry wait was interrupted", interrupted);
        }
    }
}
