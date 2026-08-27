import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static class TokenBucket {
        private double tokens;
        private long lastRefillTime;
        private final double capacity;
        private final double refillRatePerSecond;

        TokenBucket(double capacity, double requestsPerMinute) {
            this.capacity = capacity;
            this.refillRatePerSecond = requestsPerMinute / 60.0;
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume(int tokensRequested) {
            refill();
            if (tokens >= tokensRequested) {
                tokens -= tokensRequested;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsedMillis = now - lastRefillTime;
            double tokensToAdd = (elapsedMillis / 1000.0) * refillRatePerSecond;
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }
    }

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final double requestsPerMinute;
    private final double capacity;

    public RateLimiter(double requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
        this.capacity = requestsPerMinute;
    }

    public boolean allowRequest(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId,
            key -> new TokenBucket(capacity, requestsPerMinute));
        return bucket.tryConsume(1);
    }

    public void cleanup() {
        // Optionally remove old buckets after a timeout period
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry -> (now - entry.getValue().lastRefillTime) > 3600000);
    }
}
