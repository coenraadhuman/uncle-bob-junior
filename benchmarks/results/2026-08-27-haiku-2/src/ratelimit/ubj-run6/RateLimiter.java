import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static class TokenBucket {
        long lastRefillTime;
        double tokens;

        TokenBucket() {
            this.lastRefillTime = System.currentTimeMillis();
            this.tokens = 0;
        }
    }

    private final double tokensPerSecond;
    private final double maxTokens;
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(int requestsPerMinute) {
        this.tokensPerSecond = requestsPerMinute / 60.0;
        this.maxTokens = requestsPerMinute;
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        TokenBucket bucket = buckets.compute(clientId, (id, existing) -> {
            if (existing == null) {
                return new TokenBucket();
            }
            double elapsedSeconds = (now - existing.lastRefillTime) / 1000.0;
            existing.tokens = Math.min(maxTokens, existing.tokens + elapsedSeconds * tokensPerSecond);
            existing.lastRefillTime = now;
            return existing;
        });

        if (bucket.tokens >= 1.0) {
            bucket.tokens -= 1.0;
            return true;
        }
        return false;
    }
}
