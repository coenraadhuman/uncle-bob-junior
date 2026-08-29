import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final long WINDOW_MILLIS = 60_000; // 1 minute
    private static final int MAX_REQUESTS = 10;
    
    private final Map<String, ClientQuota> quotas = new ConcurrentHashMap<>();
    private final long cleanupIntervalMillis = 120_000; // 2 minutes
    private volatile long lastCleanup = System.currentTimeMillis();
    
    public boolean allowRequest(String clientIp) {
        cleanupIfNeeded();
        ClientQuota quota = quotas.compute(clientIp, (ip, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new ClientQuota();
            }
            return existing;
        });
        return quota.tryConsume();
    }
    
    private void cleanupIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < cleanupIntervalMillis) {
            return;
        }
        synchronized (this) {
            now = System.currentTimeMillis();
            if (now - lastCleanup < cleanupIntervalMillis) {
                return;
            }
            quotas.entrySet().removeIf(e -> e.getValue().isExpired());
            lastCleanup = now;
        }
    }
    
    private static class ClientQuota {
        private long windowStartMillis = System.currentTimeMillis();
        private int requestCount = 0;
        
        synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            if (now - windowStartMillis > WINDOW_MILLIS) {
                windowStartMillis = now;
                requestCount = 0;
            }
            if (requestCount < MAX_REQUESTS) {
                requestCount++;
                return true;
            }
            return false;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - windowStartMillis > WINDOW_MILLIS;
        }
    }
}
