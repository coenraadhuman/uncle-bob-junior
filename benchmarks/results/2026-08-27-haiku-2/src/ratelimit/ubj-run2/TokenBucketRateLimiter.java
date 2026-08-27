public class TokenBucketRateLimiter {
    private static final double TOKENS_PER_SECOND = 1.0 / 6.0;  // 10/min = ~0.167/sec
    private static final double CAPACITY = 10.0;

    private double tokens;
    private long lastRefillTimeMs;

    public TokenBucketRateLimiter() {
        this.tokens = CAPACITY;
        this.lastRefillTimeMs = System.currentTimeMillis();
    }

    public synchronized boolean allowRequest() {
        refill();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsedMs = now - lastRefillTimeMs;
        double tokensToAdd = (elapsedMs / 1000.0) * TOKENS_PER_SECOND;
        tokens = Math.min(CAPACITY, tokens + tokensToAdd);
        lastRefillTimeMs = now;
    }
}
