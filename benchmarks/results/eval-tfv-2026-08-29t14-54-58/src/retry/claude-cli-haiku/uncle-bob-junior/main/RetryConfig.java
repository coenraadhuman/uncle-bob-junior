public record RetryConfig(int maxAttempts, long retryDelayMs) {
    public RetryConfig {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be at least 1");
        if (retryDelayMs < 0) throw new IllegalArgumentException("retryDelayMs must be non-negative");
    }
}
