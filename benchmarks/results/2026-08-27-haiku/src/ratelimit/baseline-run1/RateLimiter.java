import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static class TokenBucket {
        private final long maxTokens;
        private final long refillRate; // tokens per millisecond
        private double tokens;
        private long lastRefillTime;

        TokenBucket(long maxTokens, long refillIntervalMs) {
            this.maxTokens = maxTokens;
            this.refillRate = maxTokens / (double) refillIntervalMs;
            this.tokens = maxTokens;
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume(int tokensNeeded) {
            refill();
            if (tokens >= tokensNeeded) {
                tokens -= tokensNeeded;
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

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final long maxRequests;
    private final long windowMs;
    private final ScheduledExecutorService cleanupExecutor;

    public RateLimiter(long maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
        this.cleanupExecutor = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "RateLimiter-Cleanup");
            t.setDaemon(true);
            return t;
        });
        // Clean up old entries periodically
        cleanupExecutor.scheduleAtFixedRate(this::cleanup, windowMs, windowMs, TimeUnit.MILLISECONDS);
    }

    public boolean allowRequest(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId,
            k -> new TokenBucket(maxRequests, windowMs));
        return bucket.tryConsume(1);
    }

    private void cleanup() {
        // Remove entries that haven't been used (optional, keeps memory bounded)
        // In production, you might want a more sophisticated eviction policy
    }

    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}
