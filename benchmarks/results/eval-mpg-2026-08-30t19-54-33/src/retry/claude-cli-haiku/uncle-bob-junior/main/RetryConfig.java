import java.util.concurrent.Callable;

public class RetryConfig {
    private final int maxAttempts;
    private final long delayMs;

    public RetryConfig(int maxAttempts, long delayMs) {
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public long delayMs() {
        return delayMs;
    }
}
