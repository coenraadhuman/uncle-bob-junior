import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static class ClientQuota {
        private double tokens;
        private long lastRefillTime;
        
        ClientQuota(int capacity) {
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }
    }
    
    private final int capacity;
    private final int refillRatePerMinute;
    private final ConcurrentHashMap<String, ClientQuota> quotas;
    private final long refillIntervalMs;
    
    public RateLimiter(int requestsPerMinute) {
        this.capacity = requestsPerMinute;
        this.refillRatePerMinute = requestsPerMinute;
        this.quotas = new ConcurrentHashMap<>();
        this.refillIntervalMs = 60_000L / requestsPerMinute;
    }
    
    public synchronized boolean allowRequest(String clientId) {
        ClientQuota quota = quotas.computeIfAbsent(clientId, k -> new ClientQuota(capacity));
        
        long now = System.currentTimeMillis();
        long timePassed = now - quota.lastRefillTime;
        
        quota.tokens += (double) timePassed / refillIntervalMs;
        if (quota.tokens > capacity) {
            quota.tokens = capacity;
        }
        quota.lastRefillTime = now;
        
        if (quota.tokens >= 1.0) {
            quota.tokens -= 1.0;
            return true;
        }
        return false;
    }
    
    public void cleanup() {
        long now = System.currentTimeMillis();
        quotas.entrySet().removeIf(entry -> 
            (now - entry.getValue().lastRefillTime) > 300_000
        );
    }
}
