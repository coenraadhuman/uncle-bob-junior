import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = 60_000; // 1 minute
    
    private final ConcurrentHashMap<String, Deque<Long>> requestTimes = new ConcurrentHashMap<>();
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> times = requestTimes.computeIfAbsent(clientId, k -> new ConcurrentLinkedDeque<>());
        
        // Remove timestamps outside the window
        while (!times.isEmpty() && times.peekFirst() < now - WINDOW_MILLIS) {
            times.pollFirst();
        }
        
        if (times.size() < MAX_REQUESTS) {
            times.addLast(now);
            return true;
        }
        return false;
    }
    
    public int getRemainingRequests(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> times = requestTimes.getOrDefault(clientId, new ConcurrentLinkedDeque<>());
        
        while (!times.isEmpty() && times.peekFirst() < now - WINDOW_MILLIS) {
            times.pollFirst();
        }
        
        return Math.max(0, MAX_REQUESTS - times.size());
    }
}
