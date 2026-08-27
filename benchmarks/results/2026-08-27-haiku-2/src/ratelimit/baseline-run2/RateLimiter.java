import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final long WINDOW_SIZE_MS = 60_000; // 1 minute
    private final int maxRequests;
    private final Map<String, ConcurrentLinkedQueue<Long>> clientRequests;
    private final ConcurrentHashMap<String, Object> locks;

    public RateLimiter(int maxRequests) {
        this.maxRequests = maxRequests;
        this.clientRequests = new ConcurrentHashMap<>();
        this.locks = new ConcurrentHashMap<>();
    }

    public boolean isAllowed(String clientId) {
        long now = System.currentTimeMillis();
        
        // Get or create lock for this client
        Object lock = locks.computeIfAbsent(clientId, k -> new Object());
        
        synchronized (lock) {
            ConcurrentLinkedQueue<Long> requests = clientRequests
                .computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<>());
            
            // Remove requests outside the window
            while (!requests.isEmpty() && requests.peek() < now - WINDOW_SIZE_MS) {
                requests.poll();
            }
            
            // Check if limit exceeded
            if (requests.size() < maxRequests) {
                requests.offer(now);
                return true;
            }
            return false;
        }
    }

    public int getRemainingRequests(String clientId) {
        long now = System.currentTimeMillis();
        Object lock = locks.computeIfAbsent(clientId, k -> new Object());
        
        synchronized (lock) {
            ConcurrentLinkedQueue<Long> requests = clientRequests
                .getOrDefault(clientId, new ConcurrentLinkedQueue<>());
            
            while (!requests.isEmpty() && requests.peek() < now - WINDOW_SIZE_MS) {
                requests.poll();
            }
            
            return Math.max(0, maxRequests - requests.size());
        }
    }
}
