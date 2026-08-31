import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RateLimiter {
    private final int maxRequests;
    private final long windowMs;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Long>> requestTimestamps;

    public RateLimiter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
        this.requestTimestamps = new ConcurrentHashMap<>();
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        CopyOnWriteArrayList<Long> timestamps = requestTimestamps.computeIfAbsent(
            clientId,
            k -> new CopyOnWriteArrayList<>()
        );

        timestamps.removeIf(timestamp -> now - timestamp > windowMs);

        if (timestamps.size() < maxRequests) {
            timestamps.add(now);
            return true;
        }
        return false;
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        requestTimestamps.forEach((clientId, timestamps) -> {
            timestamps.removeIf(timestamp -> now - timestamp > windowMs);
            if (timestamps.isEmpty()) {
                requestTimestamps.remove(clientId);
            }
        });
    }
}
