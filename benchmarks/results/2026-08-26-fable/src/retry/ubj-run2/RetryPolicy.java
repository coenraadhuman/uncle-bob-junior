package com.example.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Runs an operation and retries it with a fixed delay when it throws.
 * Immutable and thread-safe: build one policy and reuse it.
 *
 * <pre>
 *   RetryPolicy policy = RetryPolicy.of(3, Duration.ofMillis(200));
 *   String body = policy.execute(() -> httpClient.fetch(url));
 * </pre>
 */
public final class RetryPolicy {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;

    private RetryPolicy(int maxAttempts, Duration delayBetweenAttempts) {
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    public static RetryPolicy of(int maxAttempts, Duration delayBetweenAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, was " + maxAttempts);
        }
        Objects.requireNonNull(delayBetweenAttempts, "delayBetweenAttempts");
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException(
                    "delayBetweenAttempts must not be negative, was " + delayBetweenAttempts);
        }
        return new RetryPolicy(maxAttempts, delayBetweenAttempts);
    }

    /** Runs {@code operation}, retrying on failure; returns its result or rethrows the last failure. */
    public <T> T execute(Callable<T> operation) throws Exception {
        Objects.requireNonNull(operation, "operation");
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (InterruptedException interruption) {
                // An interrupted operation must not be retried; preserve the interrupt contract.
                Thread.currentThread().interrupt();
                throw withSuppressed(interruption, lastFailure);
            } catch (Exception failure) {
                lastFailure = withSuppressed(failure, lastFailure);
                if (attempt < maxAttempts) {
                    sleepBeforeNextAttempt(lastFailure);
                }
            }
        }
        throw lastFailure;
    }

    /** Overload for operations without a return value. */
    public void execute(ThrowingRunnable operation) throws Exception {
        Objects.requireNonNull(operation, "operation");
        execute(() -> {
            operation.run();
            return null;
        });
    }

    private void sleepBeforeNextAttempt(Exception lastFailure) throws InterruptedException {
        try {
            Thread.sleep(delayBetweenAttempts.toMillis());
        } catch (InterruptedException interruption) {
            Thread.currentThread().interrupt();
            throw withSuppressed(interruption, lastFailure);
        }
    }

    /** Keeps every earlier attempt's failure visible on the exception that finally escapes. */
    private static <E extends Exception> E withSuppressed(E current, Exception previous) {
        if (previous != null) {
            current.addSuppressed(previous);
        }
        return current;
    }

    /** Like {@link Runnable} but allowed to throw checked exceptions. */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
