import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int MAX_REQUESTS = 5;
    private static final long TIME_WINDOW_MS = 60_000; // 1 minute
    
    private final ConcurrentHashMap<String, Deque<Long>> clientRequests = new ConcurrentHashMap<>();
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        
        clientRequests.putIfAbsent(clientId, new ConcurrentLinkedDeque<>());
        Deque<Long> timestamps = clientRequests.get(clientId);
        
        // Remove old timestamps outside the time window
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - TIME_WINDOW_MS) {
            timestamps.pollFirst();
        }
        
        // Check if limit exceeded
        if (timestamps.size() >= MAX_REQUESTS) {
            return false;
        }
        
        // Record this request
        timestamps.addLast(now);
        return true;
    }
    
    public int getRemainingRequests(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequests.getOrDefault(clientId, new ConcurrentLinkedDeque<>());
        
        long validCount = timestamps.stream()
            .filter(ts -> ts >= now - TIME_WINDOW_MS)
            .count();
        
        return (int) Math.max(0, MAX_REQUESTS - validCount);
    }
}
