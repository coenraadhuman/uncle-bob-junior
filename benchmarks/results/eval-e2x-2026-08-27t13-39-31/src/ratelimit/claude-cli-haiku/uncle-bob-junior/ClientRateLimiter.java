import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class ClientRateLimiter {
    private final int maxRequests;
    private final long windowSeconds;
    private final ConcurrentHashMap<String, TokenBucket> buckets;
    private final ScheduledExecutorService cleanup;

    public ClientRateLimiter(int maxRequests, long windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
        this.buckets = new ConcurrentHashMap<>();
        this.cleanup = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "RateLimiter-Cleanup");
            t.setDaemon(true);
            return t;
        });
        scheduleCleanup();
    }

    public boolean allowRequest(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, _ -> new TokenBucket(maxRequests, windowSeconds));
        return bucket.consumeToken();
    }

    private void scheduleCleanup() {
        cleanup.scheduleAtFixedRate(this::removeExpiredBuckets, windowSeconds, windowSeconds, TimeUnit.SECONDS);
    }

    private void removeExpiredBuckets() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    public void shutdown() {
        cleanup.shutdown();
    }

    private static class TokenBucket {
        private final int capacity;
        private final double refillRate;
        private double tokens;
        private long lastRefillTime;

        TokenBucket(int capacity, long windowSeconds) {
            this.capacity = capacity;
            this.refillRate = (double) capacity / windowSeconds;
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean consumeToken() {
            refill();
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsedMs = now - lastRefillTime;
            double tokensToAdd = (elapsedMs / 1000.0) * refillRate;
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }

        boolean isExpired(long now) {
            return (now - lastRefillTime) > 5 * 60 * 1000;
        }
    }
}
