// RetryConfig.java
import java.time.Duration;

/**
 * Immutable retry policy: how many attempts to make and how long to wait
 * between them.
 *
 * @param maxAttempts total number of attempts, including the first one (must be >= 1)
 * @param delay       fixed wait time between attempts (must not be negative)
 */
public record RetryConfig(int maxAttempts, Duration delay) {

    public RetryConfig {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative");
        }
    }
}
