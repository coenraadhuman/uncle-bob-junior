public class TokenBucketRateLimiter {
    private final double tokensPerSecond;
    private final double maxTokens;
    private final ConcurrentHashMap<String, ClientBucket> buckets;
    
    private static class ClientBucket {
        double tokens;
        long lastRefillTime;
        
        ClientBucket(double maxTokens) {
            this.tokens = maxTokens;
            this.lastRefillTime = System.nanoTime();
        }
    }
    
    public TokenBucketRateLimiter(double tokensPerSecond, double maxTokens) {
        this.tokensPerSecond = tokensPerSecond;
        this.maxTokens = maxTokens;
        this.buckets = new ConcurrentHashMap<>();
    }
    
    public boolean allowRequest(String clientId, int tokensRequested) {
        ClientBucket bucket = buckets.computeIfAbsent(clientId, 
            k -> new ClientBucket(maxTokens));
        
        synchronized (bucket) {
            long now = System.nanoTime();
            double elapsedSeconds = (now - bucket.lastRefillTime) / 1_000_000_000.0;
            bucket.tokens = Math.min(maxTokens, bucket.tokens + elapsedSeconds * tokensPerSecond);
            bucket.lastRefillTime = now;
            
            if (bucket.tokens >= tokensRequested) {
                bucket.tokens -= tokensRequested;
                return true;
            }
            return false;
        }
    }
}
