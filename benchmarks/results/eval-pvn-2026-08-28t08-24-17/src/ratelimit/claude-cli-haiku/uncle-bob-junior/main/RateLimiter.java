import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RateLimiter {
    private static final int DEFAULT_MAX_REQUESTS = 10;
    private static final long DEFAULT_WINDOW_MILLIS = 60_000;
    
    private final Map<String, Deque<Long>> clientTimestamps = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long windowMillis;
    
    public RateLimiter() {
        this(DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_MILLIS);
    }
    
    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }
    
    public boolean isAllowed(String clientId) {
        long now = System.currentTimeMillis();
        
        Deque<Long> timestamps = clientTimestamps.computeIfAbsent(
            clientId,
            k -> new ConcurrentLinkedDeque<>()
        );
        
        removeExpiredTimestamps(timestamps, now);
        
        if (timestamps.size() >= maxRequests) {
            return false;
        }
        
        timestamps.addLast(now);
        return true;
    }
    
    private void removeExpiredTimestamps(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && isOlderThanWindow(timestamps.peekFirst(), now)) {
            timestamps.removeFirst();
        }
    }
    
    private boolean isOlderThanWindow(long timestamp, long now) {
        return now - timestamp >= windowMillis;
    }
}
