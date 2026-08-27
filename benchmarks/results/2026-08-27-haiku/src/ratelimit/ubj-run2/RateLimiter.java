import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int DEFAULT_REQUESTS_PER_MINUTE = 10;
    private static final long MINUTE_IN_MILLIS = 60_000;
    
    private final int requestsPerMinute;
    private final ConcurrentHashMap<String, TokenBucket> buckets;
    
    public RateLimiter() {
        this(DEFAULT_REQUESTS_PER_MINUTE);
    }
    
    public RateLimiter(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
        this.buckets = new ConcurrentHashMap<>();
    }
    
    public boolean allowRequest(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(
            clientId,
            k -> new TokenBucket(requestsPerMinute)
        );
        return bucket.tryConsume();
    }
    
    private static class TokenBucket {
        private final int capacity;
        private double tokens;
        private long lastRefillTime;
        
        TokenBucket(int capacity) {
            this.capacity = capacity;
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }
        
        synchronized boolean tryConsume() {
            refillTokens();
            
            if (tokens >= 1) {
                tokens--;
                return true;
            }
            return false;
        }
        
        private void refillTokens() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;
            double tokensToAdd = (timePassed / (double) MINUTE_IN_MILLIS) * capacity;
            
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }
    }
}
