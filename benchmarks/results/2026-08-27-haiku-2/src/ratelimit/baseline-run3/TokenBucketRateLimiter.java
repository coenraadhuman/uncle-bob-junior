public class TokenBucketRateLimiter {
    private final int capacity;
    private final double refillRate; // tokens per millisecond
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(int capacity, long refillIntervalMillis) {
        this.capacity = capacity;
        this.refillRate = (double) capacity / refillIntervalMillis;
    }

    public boolean allowRequest(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, k -> new TokenBucket());
        return bucket.tryConsume(1);
    }

    private class TokenBucket {
        private double tokens;
        private long lastRefillTime = System.currentTimeMillis();

        synchronized boolean tryConsume(int needed) {
            refill();
            if (tokens >= needed) {
                tokens -= needed;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;
            tokens = Math.min(capacity, tokens + (timePassed * refillRate));
            lastRefillTime = now;
        }
    }
}
