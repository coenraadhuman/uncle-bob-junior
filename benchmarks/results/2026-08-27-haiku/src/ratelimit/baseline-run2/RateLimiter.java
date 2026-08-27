import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiter {
    private final int requestsPerMinute;
    private final ConcurrentHashMap<String, ClientQuota> quotas = new ConcurrentHashMap<>();

    public RateLimiter(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        ClientQuota quota = quotas.compute(clientId, (key, existing) -> {
            if (existing == null) {
                existing = new ClientQuota();
            }
            existing.cleanup(now);
            return existing;
        });

        synchronized (quota) {
            // Remove requests older than 1 minute
            quota.requests.removeIf(timestamp -> now - timestamp > 60_000);

            if (quota.requests.size() < requestsPerMinute) {
                quota.requests.add(now);
                return true;
            }
            return false;
        }
    }

    private static class ClientQuota {
        final java.util.LinkedList<Long> requests = new java.util.LinkedList<>();

        void cleanup(long now) {
            if (!requests.isEmpty() && now - requests.getFirst() > 120_000) {
                // Clean up if oldest request is older than 2 minutes
                requests.clear();
            }
        }
    }
}
