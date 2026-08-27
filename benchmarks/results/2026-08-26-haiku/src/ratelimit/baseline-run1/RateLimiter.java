import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static class ClientQuota {
        private final Queue<Long> requestTimestamps;
        private final int maxRequests;
        private final long windowMillis;

        ClientQuota(int maxRequests, long windowMillis) {
            this.maxRequests = maxRequests;
            this.windowMillis = windowMillis;
            this.requestTimestamps = new ConcurrentLinkedQueue<>();
        }

        synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            
            // Remove timestamps outside the window
            while (!requestTimestamps.isEmpty() && 
                   requestTimestamps.peek() < now - windowMillis) {
                requestTimestamps.poll();
            }

            // Check if under limit
            if (requestTimestamps.size() < maxRequests) {
                requestTimestamps.offer(now);
                return true;
            }
            return false;
        }
    }

    private final ConcurrentHashMap<String, ClientQuota> quotas;
    private final int maxRequests;
    private final long windowMillis;
    private final Timer cleanupTimer;

    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.quotas = new ConcurrentHashMap<>();
        
        // Cleanup stale entries every 5 minutes
        this.cleanupTimer = new Timer(true);
        this.cleanupTimer.scheduleAtFixedRate(this::cleanup, 300000, 300000);
    }

    public boolean isAllowed(String clientId) {
        return quotas.computeIfAbsent(clientId, k -> 
            new ClientQuota(maxRequests, windowMillis)
        ).allowRequest();
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        quotas.entrySet().removeIf(entry -> {
            Queue<Long> timestamps = entry.getValue().requestTimestamps;
            return timestamps.isEmpty() || 
                   (timestamps.peek() < now - windowMillis);
        });
    }

    public void shutdown() {
        cleanupTimer.cancel();
    }
}
