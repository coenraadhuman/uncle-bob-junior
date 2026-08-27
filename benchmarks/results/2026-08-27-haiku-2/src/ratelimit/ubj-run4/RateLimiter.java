import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int REQUESTS_PER_MINUTE = 5;
    private static final long MINUTE_IN_MILLIS = 60_000;

    private final Map<String, Deque<Long>> requestTimestamps;
    private final int maxRequests;
    private final long windowMillis;

    public RateLimiter() {
        this(REQUESTS_PER_MINUTE, MINUTE_IN_MILLIS);
    }

    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.requestTimestamps = new ConcurrentHashMap<>();
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = requestTimestamps.computeIfAbsent(
            clientId,
            k -> new ArrayDeque<>()
        );

        removeExpiredTimestamps(timestamps, now);

        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        }

        return false;
    }

    private void removeExpiredTimestamps(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - windowMillis) {
            timestamps.removeFirst();
        }
    }
}
