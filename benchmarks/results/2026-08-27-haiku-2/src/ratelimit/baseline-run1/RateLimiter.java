import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Deque<Long>> clientRequests;
    
    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.clientRequests = new ConcurrentHashMap<>();
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequests.computeIfAbsent(clientId, k -> new ConcurrentLinkedDeque<>());
        
        // Remove timestamps outside the window
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - windowMillis) {
            timestamps.removeFirst();
        }
        
        // Check if limit exceeded
        if (timestamps.size() >= maxRequests) {
            return false;
        }
        
        // Add current request
        timestamps.addLast(now);
        return true;
    }
    
    public int getRemainingRequests(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequests.get(clientId);
        
        if (timestamps == null) {
            return maxRequests;
        }
        
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < now - windowMillis) {
                timestamps.removeFirst();
            }
            return Math.max(0, maxRequests - timestamps.size());
        }
    }
}
