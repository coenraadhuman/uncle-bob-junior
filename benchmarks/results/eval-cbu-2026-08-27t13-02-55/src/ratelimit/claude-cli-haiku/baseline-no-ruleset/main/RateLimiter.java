import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_SIZE_MS = 60_000; // 1 minute
    private static final long CLEANUP_INTERVAL_MS = 60_000;
    
    private final ConcurrentHashMap<String, Queue<Long>> requestTimestamps = 
        new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = 
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "RateLimiter-Cleanup");
            t.setDaemon(true);
            return t;
        });
    
    public RateLimiter() {
        // Clean up old entries periodically
        cleanupExecutor.scheduleAtFixedRate(this::cleanup, 
            CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Queue<Long> timestamps = requestTimestamps.computeIfAbsent(clientId, 
            k -> new ConcurrentLinkedQueue<>());
        
        // Remove timestamps outside the window
        while (!timestamps.isEmpty() && timestamps.peek() < now - WINDOW_SIZE_MS) {
            timestamps.poll();
        }
        
        // Check if limit exceeded
        if (timestamps.size() >= MAX_REQUESTS) {
            return false;
        }
        
        // Add current timestamp
        timestamps.offer(now);
        return true;
    }
    
    private void cleanup() {
        long now = System.currentTimeMillis();
        requestTimestamps.entrySet().removeIf(entry -> {
            Queue<Long> queue = entry.getValue();
            queue.removeIf(timestamp -> timestamp < now - WINDOW_SIZE_MS);
            return queue.isEmpty();
        });
    }
    
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}
