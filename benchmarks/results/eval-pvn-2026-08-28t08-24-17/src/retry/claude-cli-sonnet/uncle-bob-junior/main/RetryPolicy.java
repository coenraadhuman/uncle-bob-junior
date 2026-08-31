package retry;

import java.time.Duration;

public record RetryPolicy(int maxAttempts, Duration delayBetweenAttempts) {

    public RetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative");
        }
    }
}
