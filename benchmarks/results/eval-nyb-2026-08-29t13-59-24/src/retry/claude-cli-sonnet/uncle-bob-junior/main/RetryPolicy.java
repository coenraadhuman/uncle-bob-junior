// RetryPolicy.java
package retry;

import java.time.Duration;

/**
 * Immutable configuration for a retry operation.
 *
 * @param maxAttempts total number of attempts, including the first (must be >= 1)
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
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException(
                    "delayBetweenAttempts must not be negative, was " + delayBetweenAttempts);
        }
        return new RetryPolicy(maxAttempts, delayBetweenAttempts);
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public Duration delayBetweenAttempts() {
        return delayBetweenAttempts;
    }
}
