import java.time.Duration;

public final class RetryConfig {

    private static final int MIN_ATTEMPTS = 1;

    private final int maxAttempts;
    private final Duration delay;

    public RetryConfig(int maxAttempts, Duration delay) {
        if (maxAttempts < MIN_ATTEMPTS) {
            throw new IllegalArgumentException("maxAttempts must be at least " + MIN_ATTEMPTS);
        }
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delay = delay;
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public Duration delay() {
        return delay;
    }
}
