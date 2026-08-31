import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = 60_000;
    
    private final Map<String, Queue<Long>> requestsByClient = new ConcurrentHashMap<>();
    private final Clock clock;
    
    public RateLimiter() {
        this(System::currentTimeMillis);
    }
    
    RateLimiter(Clock clock) {
        this.clock = clock;
    }
    
    public boolean allowRequest(String clientId) {
        long now = clock.currentTimeMillis();
        Queue<Long> requests = requestsByClient.computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<>());
        
        requests.removeIf(timestamp -> now - timestamp > WINDOW_MILLIS);
        
        if (requests.size() >= MAX_REQUESTS) {
            return false;
        }
        
        requests.offer(now);
        return true;
    }
    
    @FunctionalInterface
    interface Clock {
        long currentTimeMillis();
    }
}
