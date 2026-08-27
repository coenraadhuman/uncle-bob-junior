import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RateLimiter {
    private static final int REQUESTS_PER_MINUTE = 5;
    private static final long WINDOW_SIZE_MS = 60_000; // 1 minute
    private static final long CLEANUP_INTERVAL_MS = 300_000; // 5 minutes
    
    private final Map<String, LinkedList<Long>> clientRequests = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private long lastCleanup = System.currentTimeMillis();
    
    /**
     * Check if a client is allowed to make a request.
     * @param clientIp The client's IP address
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean allowRequest(String clientIp) {
        lock.writeLock().lock();
        try {
            cleanupIfNeeded();
            
            long now = System.currentTimeMillis();
            LinkedList<Long> requests = clientRequests.computeIfAbsent(clientIp, k -> new LinkedList<>());
            
            // Remove timestamps outside the window
            while (!requests.isEmpty() && requests.getFirst() < now - WINDOW_SIZE_MS) {
                requests.removeFirst();
            }
            
            // Check if limit exceeded
            if (requests.size() >= REQUESTS_PER_MINUTE) {
                return false;
            }
            
            // Record this request
            requests.addLast(now);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Get remaining requests for a client in the current window.
     */
    public int getRemainingRequests(String clientIp) {
        lock.readLock().lock();
        try {
            long now = System.currentTimeMillis();
            LinkedList<Long> requests = clientRequests.get(clientIp);
            if (requests == null) {
                return REQUESTS_PER_MINUTE;
            }
            
            long validCount = requests.stream()
                .filter(ts -> ts >= now - WINDOW_SIZE_MS)
                .count();
            
            return Math.max(0, REQUESTS_PER_MINUTE - (int) validCount);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private void cleanupIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup > CLEANUP_INTERVAL_MS) {
            clientRequests.entrySet().removeIf(entry -> 
                entry.getValue().isEmpty() || 
                entry.getValue().getLast() < now - WINDOW_SIZE_MS
            );
            lastCleanup = now;
        }
    }
}
