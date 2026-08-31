import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int DEFAULT_MAX_REQUESTS = 10;
    private static final long DEFAULT_WINDOW_MILLIS = 60_000;
    
    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Deque<Long>> clientTimestamps = 
        new ConcurrentHashMap<>();
    
    public RateLimiter() {
        this(DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_MILLIS);
    }
    
    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }
    
    public boolean isAllowed(String clientId) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowMillis;
        
        Deque<Long> timestamps = clientTimestamps.computeIfAbsent(
            clientId, 
            k -> new ConcurrentLinkedDeque<>()
        );
        
        timestamps.removeIf(ts -> ts < windowStart);
        
        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }
}
