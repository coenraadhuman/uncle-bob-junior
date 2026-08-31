import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = 60_000;
    
    private final ConcurrentHashMap<String, Deque<Long>> clientRequests = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);
    
    public RateLimiter() {
        // Clean up old entries every 2 minutes to prevent memory leak
        cleanupExecutor.scheduleAtFixedRate(this::cleanup, 2, 2, TimeUnit.MINUTES);
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequests.computeIfAbsent(clientId, k -> new ConcurrentLinkedDeque<>());
        
        // Remove timestamps outside the window
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - WINDOW_MILLIS) {
            timestamps.pollFirst();
        }
        
        // Check if limit exceeded
        if (timestamps.size() >= MAX_REQUESTS) {
            return false;
        }
        
        timestamps.addLast(now);
        return true;
    }
    
    private void cleanup() {
        long now = System.currentTimeMillis();
        clientRequests.entrySet().removeIf(entry -> {
            Deque<Long> timestamps = entry.getValue();
            while (!timestamps.isEmpty() && timestamps.peekFirst() < now - WINDOW_MILLIS) {
                timestamps.pollFirst();
            }
            return timestamps.isEmpty();
        });
    }
    
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}
