import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long MINUTE_IN_MILLIS = 60_000;
    
    private final Map<String, Deque<Long>> clientRequestTimes;
    private final int maxRequests;
    private final long windowMillis;
    
    public RateLimiter(int maxRequests, long windowMillis) {
        this.clientRequestTimes = new ConcurrentHashMap<>();
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }
    
    public RateLimiter() {
        this(MAX_REQUESTS_PER_MINUTE, MINUTE_IN_MILLIS);
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequestTimes.computeIfAbsent(clientId, k -> new ConcurrentLinkedDeque<>());
        
        removeExpiredRequests(timestamps, now);
        
        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }
    
    private void removeExpiredRequests(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - windowMillis) {
            timestamps.removeFirst();
        }
    }
    
    public int getRemainingRequests(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequestTimes.getOrDefault(clientId, new ConcurrentLinkedDeque<>());
        
        removeExpiredRequests(timestamps, now);
        return Math.max(0, maxRequests - timestamps.size());
    }
}
