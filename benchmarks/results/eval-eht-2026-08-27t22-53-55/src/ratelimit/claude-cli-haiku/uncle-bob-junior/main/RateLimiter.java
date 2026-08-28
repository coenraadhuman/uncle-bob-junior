import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MILLIS = 60_000; // 1 minute
    
    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, ClientRequestHistory> history;
    
    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.history = new ConcurrentHashMap<>();
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        ClientRequestHistory clientHistory = history.computeIfAbsent(clientId,
            _ -> new ClientRequestHistory());
        
        return clientHistory.recordRequest(now, maxRequests, windowMillis);
    }
    
    private static class ClientRequestHistory {
        private final Queue<Long> timestamps = new ConcurrentLinkedQueue<>();
        
        synchronized boolean recordRequest(long now, int maxRequests, long windowMillis) {
            timestamps.removeIf(ts -> now - ts > windowMillis);
            
            if (timestamps.size() >= maxRequests) {
                return false;
            }
            
            timestamps.offer(now);
            return true;
        }
    }
}
