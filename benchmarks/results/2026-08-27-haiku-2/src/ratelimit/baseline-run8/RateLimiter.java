import java.util.concurrent.*;
import java.util.*;

public class RateLimiter {
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_SIZE_MS = 60_000; // 1 minute
    
    private static class ClientQuota {
        private final Queue<Long> requestTimestamps = new LinkedList<>();
        private final Object lock = new Object();
        
        boolean allowRequest() {
            synchronized (lock) {
                long now = System.currentTimeMillis();
                
                // Remove timestamps older than the window
                requestTimestamps.removeIf(ts -> now - ts > WINDOW_SIZE_MS);
                
                // Check if limit exceeded
                if (requestTimestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
                    return false;
                }
                
                requestTimestamps.add(now);
                return true;
            }
        }
    }
    
    private final ConcurrentHashMap<String, ClientQuota> quotas = new ConcurrentHashMap<>();
    
    public boolean allowRequest(String clientIdentifier) {
        ClientQuota quota = quotas.computeIfAbsent(
            clientIdentifier, 
            k -> new ClientQuota()
        );
        return quota.allowRequest();
    }
    
    public void resetClient(String clientIdentifier) {
        quotas.remove(clientIdentifier);
    }
}
