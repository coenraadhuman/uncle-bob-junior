import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static class ClientQuota {
        long tokens;
        long lastRefillTime;
        long lastAccessTime;

        ClientQuota() {
            this.tokens = MAX_REQUESTS;
            this.lastRefillTime = System.currentTimeMillis();
            this.lastAccessTime = System.currentTimeMillis();
        }
    }

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MILLIS = 60_000; // 60 seconds
    private static final long CLEANUP_INTERVAL = 300_000; // 5 minutes
    private static final long ENTRY_EXPIRY = 600_000; // 10 minutes

    private final Map<String, ClientQuota> quotas = new ConcurrentHashMap<>();
    private long lastCleanup = System.currentTimeMillis();

    public boolean allowRequest(String clientIp) {
        long now = System.currentTimeMillis();
        
        // Cleanup stale entries periodically
        if (now - lastCleanup > CLEANUP_INTERVAL) {
            quotas.entrySet().removeIf(e -> 
                now - e.getValue().lastAccessTime > ENTRY_EXPIRY
            );
            lastCleanup = now;
        }

        ClientQuota quota = quotas.computeIfAbsent(clientIp, k -> new ClientQuota());
        quota.lastAccessTime = now;

        // Refill tokens based on elapsed time
        long elapsedMillis = now - quota.lastRefillTime;
        long tokensToAdd = (elapsedMillis / WINDOW_MILLIS) * MAX_REQUESTS;
        
        if (tokensToAdd > 0) {
            quota.tokens = Math.min(MAX_REQUESTS, quota.tokens + tokensToAdd);
            quota.lastRefillTime += (tokensToAdd / MAX_REQUESTS) * WINDOW_MILLIS;
        }

        // Allow request if tokens available
        if (quota.tokens > 0) {
            quota.tokens--;
            return true;
        }
        return false;
    }
}
