import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private final int maxRequests;
    private final long windowMs = 60_000; // 1 minute
    private final ConcurrentHashMap<String, Deque<Long>> requestTimestamps;
    
    public RateLimiter(int maxRequestsPerMinute) {
        this.maxRequests = maxRequestsPerMinute;
        this.requestTimestamps = new ConcurrentHashMap<>();
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = requestTimestamps.computeIfAbsent(clientId, k -> new ConcurrentLinkedDeque<>());
        
        // Remove timestamps outside the sliding window
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMs) {
            timestamps.removeFirst();
        }
        
        // Allow if under limit
        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }
}
