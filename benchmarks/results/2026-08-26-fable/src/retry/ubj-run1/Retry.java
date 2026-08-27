package com.example.retry;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Runs an operation, retrying on any exception up to a maximum number of
 * attempts, with a fixed delay between attempts.
 *
 * <p>Usage:
 * <pre>{@code
 * Retry retry = Retry.of(3, Duration.ofMillis(200));
 * String body = retry.execute(() -> httpClient.fetch(url));
 * }</pre>
 */
public final class Retry {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    private Retry(int maxAttempts, Duration delayBetweenAttempts) {
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    public static Retry of(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, was " + maxAttempts);
        }
        if (delayBetweenAttempts == null || delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must be zero or positive");
        }
        return new Retry(maxAttempts, delayBetweenAttempts);
    }

    /** Runs the operation and returns its result, retrying on any exception. */
    public <T> T execute(Callable<T> operation) {
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
            }
            if (attempt < maxAttempts) {
                waitBeforeNextAttempt(lastFailure);
            }
        }
        throw new RetryException("Operation failed after " + maxAttempts + " attempt(s)", lastFailure);
    }

    /** Runs an operation that returns no result, retrying on any exception. */
    public void execute(ThrowingRunnable operation) {
        execute(() -> {
            operation.run();
            return null;
        });
    }

    private void waitBeforeNextAttempt(Exception lastFailure) {
        try {
            Thread.sleep(delayBetweenAttempts.toMillis());
        } catch (InterruptedException interrupt) {
            Thread.currentThread().interrupt();
            RetryException aborted =
                    new RetryException("Retry aborted: interrupted while waiting between attempts", interrupt);
            aborted.addSuppressed(lastFailure);
            throw aborted;
        }
    }

    /** A no-result operation that may throw a checked exception. */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
