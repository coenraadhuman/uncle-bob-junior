import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static class ClientRequests {
        private final Deque<Long> timestamps = new LinkedList<>();
        
        synchronized boolean allowRequest(int maxRequests, long windowMillis) {
            long now = System.currentTimeMillis();
            
            // Remove timestamps outside the window
            while (!timestamps.isEmpty() && timestamps.peekFirst() < now - windowMillis) {
                timestamps.removeFirst();
            }
            
            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                return true;
            }
            return false;
        }
    }
    
    private final Map<String, ClientRequests> clients = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long windowMillis;
    
    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        
        // Cleanup thread for stale entries (removes clients with no requests in 10 minutes)
        new Timer(true).scheduleAtFixedRate(() -> {
            long cutoff = System.currentTimeMillis() - 600_000;
            clients.entrySet().removeIf(e -> {
                Deque<Long> timestamps = e.getValue().timestamps;
                synchronized (e.getValue()) {
                    return timestamps.isEmpty() || timestamps.peekLast() < cutoff;
                }
            });
        }, 60_000, 60_000);
    }
    
    public boolean allowRequest(String clientId) {
        return clients.computeIfAbsent(clientId, k -> new ClientRequests())
                .allowRequest(maxRequests, windowMillis);
    }
}
