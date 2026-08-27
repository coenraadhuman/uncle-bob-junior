import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TokenBucketRateLimiter {
    private static class TokenBucket {
        private final long maxTokens;
        private final long refillRate; // tokens per second
        private AtomicLong tokens;
        private volatile long lastRefillTime;

        TokenBucket(long maxTokens, long refillRate) {
            this.maxTokens = maxTokens;
            this.refillRate = refillRate;
            this.tokens = new AtomicLong(maxTokens);
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean allowRequest() {
            refillTokens();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        private void refillTokens() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;
            long tokensToAdd = (timePassed * refillRate) / 1000;
            
            if (tokensToAdd > 0) {
                tokens.set(Math.min(maxTokens, tokens.get() + tokensToAdd));
                lastRefillTime = now;
            }
        }
    }

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final long maxTokens;
    private final long refillRate;

    public TokenBucketRateLimiter(long requestsPerMinute) {
        this.maxTokens = requestsPerMinute;
        this.refillRate = requestsPerMinute / 60; // convert to per-second
    }

    public boolean isAllowed(String clientId) {
        return buckets.computeIfAbsent(clientId, k -> 
            new TokenBucket(maxTokens, refillRate)
        ).allowRequest();
    }
}
