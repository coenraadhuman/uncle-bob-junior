import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiter {
    private static class ClientState {
        AtomicLong requestCount = new AtomicLong(0);
        long windowStartTime = System.currentTimeMillis();
        
        synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            long windowAge = now - windowStartTime;
            
            if (windowAge >= 60_000) {
                requestCount.set(1);
                windowStartTime = now;
                return true;
            }
            
            long count = requestCount.incrementAndGet();
            return count <= maxRequestsPerMinute;
        }
    }
    
    private final int maxRequestsPerMinute;
    private final ConcurrentHashMap<String, ClientState> clients = new ConcurrentHashMap<>();
    
    public RateLimiter(int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }
    
    public boolean allowRequest(String clientId) {
        return clients
            .computeIfAbsent(clientId, k -> new ClientState())
            .allowRequest();
    }
}
