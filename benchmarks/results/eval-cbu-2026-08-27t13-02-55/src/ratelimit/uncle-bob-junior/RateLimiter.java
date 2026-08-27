import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int DEFAULT_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_SIZE_MILLIS = 60_000;

    private final int requestsPerMinute;
    private final Map<String, Deque<Long>> clientRequests;

    public RateLimiter(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
        this.clientRequests = new ConcurrentHashMap<>();
    }

    public RateLimiter() {
        this(DEFAULT_REQUESTS_PER_MINUTE);
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequests.computeIfAbsent(clientId, _ -> new ConcurrentLinkedDeque<>());

        removeExpiredRequests(timestamps, now);

        if (timestamps.size() >= requestsPerMinute) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    public int remainingRequests(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequests.get(clientId);

        if (timestamps == null) {
            return requestsPerMinute;
        }

        removeExpiredRequests(timestamps, now);
        return Math.max(0, requestsPerMinute - timestamps.size());
    }

    private void removeExpiredRequests(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && timestamps.peekFirst() <= now - WINDOW_SIZE_MILLIS) {
            timestamps.removeFirst();
        }
    }

    public void reset(String clientId) {
        clientRequests.remove(clientId);
    }
}
