import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static class ClientQuota {
        private final Queue<Long> requestTimestamps;
        private final int maxRequests;
        private final long windowMillis;

        ClientQuota(int maxRequests, long windowMillis) {
            this.requestTimestamps = new ConcurrentLinkedQueue<>();
            this.maxRequests = maxRequests;
            this.windowMillis = windowMillis;
        }

        synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            long cutoff = now - windowMillis;

            // Remove timestamps outside the window
            requestTimestamps.removeIf(timestamp -> timestamp < cutoff);

            if (requestTimestamps.size() < maxRequests) {
                requestTimestamps.offer(now);
                return true;
            }
            return false;
        }
    }

    private final Map<String, ClientQuota> quotas = new ConcurrentHashMap<>();
    private final int maxRequestsPerMinute;
    private final long windowMillis;
    private final ScheduledExecutorService cleanup;

    public RateLimiter(int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.windowMillis = 60_000; // 1 minute

        // Clean up stale entries every 10 minutes
        this.cleanup = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "RateLimiter-Cleanup");
            t.setDaemon(true);
            return t;
        });
        cleanup.scheduleAtFixedRate(this::cleanupStaleEntries, 10, 10, TimeUnit.MINUTES);
    }

    public boolean allowRequest(String clientId) {
        ClientQuota quota = quotas.computeIfAbsent(
            clientId,
            k -> new ClientQuota(maxRequestsPerMinute, windowMillis)
        );
        return quota.allowRequest();
    }

    private void cleanupStaleEntries() {
        long now = System.currentTimeMillis();
        quotas.entrySet().removeIf(entry -> {
            Queue<Long> timestamps = entry.getValue().requestTimestamps;
            return timestamps.isEmpty() || 
                   (timestamps.peek() != null && timestamps.peek() < now - windowMillis);
        });
    }

    public void shutdown() {
        cleanup.shutdown();
    }
}
