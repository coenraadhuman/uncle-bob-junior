import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = 60_000;
    
    private final Map<String, Deque<Long>> clientRequests = new ConcurrentHashMap<>();
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequests.computeIfAbsent(clientId, k -> new ConcurrentLinkedDeque<>());
        
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - WINDOW_MILLIS) {
            timestamps.removeFirst();
        }
        
        if (timestamps.size() < MAX_REQUESTS) {
            timestamps.addLast(now);
            return true;
        }
        
        return false;
    }
}
