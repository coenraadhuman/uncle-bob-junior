import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final long WINDOW_MILLIS = 60_000; // 1 minute
    private final int maxRequests;
    private final Map<String, Queue<Long>> requestsByClient = new ConcurrentHashMap<>();
    
    public RateLimiter(int maxRequests) {
        this.maxRequests = maxRequests;
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Queue<Long> timestamps = requestsByClient.computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<>());
        
        // Remove requests older than the window
        while (!timestamps.isEmpty() && now - timestamps.peek() >= WINDOW_MILLIS) {
            timestamps.poll();
        }
        
        if (timestamps.size() < maxRequests) {
            timestamps.offer(now);
            return true;
        }
        
        return false;
    }
}
