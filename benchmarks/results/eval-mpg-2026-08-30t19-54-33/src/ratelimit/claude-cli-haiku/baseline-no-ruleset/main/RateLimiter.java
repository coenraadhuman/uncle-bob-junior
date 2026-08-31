import java.util.concurrent.*;
import java.util.*;

public class RateLimiter {
    private static class ClientQuota {
        private final Deque<Long> requestTimes = new ConcurrentLinkedDeque<>();
        private final long windowMs;
        private final int maxRequests;

        ClientQuota(long windowMs, int maxRequests) {
            this.windowMs = windowMs;
            this.maxRequests = maxRequests;
        }

        synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            long cutoff = now - windowMs;

            while (!requestTimes.isEmpty() && requestTimes.getFirst() <= cutoff) {
                requestTimes.removeFirst();
            }

            if (requestTimes.size() < maxRequests) {
                requestTimes.addLast(now);
                return true;
            }
            return false;
        }
    }

    private final ConcurrentHashMap<String, ClientQuota> quotas = new ConcurrentHashMap<>();
    private final long windowMs;
    private final int maxRequests;

    public RateLimiter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    public boolean allowRequest(String clientId) {
        return quotas
            .computeIfAbsent(clientId, k -> new ClientQuota(windowMs, maxRequests))
            .allowRequest();
    }
}
