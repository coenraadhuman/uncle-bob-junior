import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MILLIS = 60_000; // 1 minute
    private static final long CLEANUP_INTERVAL_MILLIS = 5 * 60_000; // 5 minutes
    
    private final ConcurrentHashMap<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();
    private volatile long lastCleanupTime = System.currentTimeMillis();
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        
        // Periodic cleanup to prevent memory leaks
        if (now - lastCleanupTime > CLEANUP_INTERVAL_MILLIS) {
            cleanup(now);
            lastCleanupTime = now;
        }
        
        Deque<Long> timestamps = requestTimestamps.computeIfAbsent(clientId, k -> new ArrayDeque<>());
        
        synchronized (timestamps) {
            // Remove timestamps outside the window
            while (!timestamps.isEmpty() && timestamps.getFirst() <= now - WINDOW_MILLIS) {
                timestamps.removeFirst();
            }
            
            // Check if limit exceeded
            if (timestamps.size() >= MAX_REQUESTS) {
                return false;
            }
            
            // Add current request timestamp
            timestamps.addLast(now);
            return true;
        }
    }
    
    private void cleanup(long now) {
        requestTimestamps.entrySet().removeIf(entry -> {
            Deque<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                return timestamps.stream().allMatch(ts -> ts <= now - WINDOW_MILLIS);
            }
        });
    }
}
