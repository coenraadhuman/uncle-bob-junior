import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token bucket rate limiter with per-client tracking
 */
public class RateLimiter {
    private static class TokenBucket {
        private double tokens;
        private long lastRefillTime;
        private final double maxTokens;
        private final double refillRate; // tokens per millisecond
        
        TokenBucket(int requestsPerMinute) {
            this.maxTokens = requestsPerMinute;
            this.tokens = requestsPerMinute;
            this.lastRefillTime = System.currentTimeMillis();
            this.refillRate = requestsPerMinute / 60000.0; // per millisecond
        }
        
        boolean allowRequest() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
        
        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;
            tokens = Math.min(maxTokens, tokens + timePassed * refillRate);
            lastRefillTime = now;
        }
    }
    
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final int requestsPerMinute;
    
    public RateLimiter(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
        // Clean up stale entries every 10 minutes
        startCleanupTask();
    }
    
    public boolean isAllowed(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, 
            k -> new TokenBucket(requestsPerMinute));
        return bucket.allowRequest();
    }
    
    private void startCleanupTask() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10 * 60 * 1000); // Every 10 minutes
                    long now = System.currentTimeMillis();
                    buckets.entrySet().removeIf(entry -> 
                        now - entry.getValue().lastRefillTime > 15 * 60 * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }
}
