import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_DURATION_MILLIS = 60_000;
    
    private final int maxRequests;
    private final long windowDurationMillis;
    private final Map<String, ClientRequestHistory> clientHistory;
    
    public RateLimiter(int maxRequests, long windowDurationMillis) {
        this.maxRequests = maxRequests;
        this.windowDurationMillis = windowDurationMillis;
        this.clientHistory = new ConcurrentHashMap<>();
    }
    
    public RateLimiter() {
        this(REQUESTS_PER_MINUTE, WINDOW_DURATION_MILLIS);
    }
    
    public boolean isAllowed(String clientId) {
        long now = System.currentTimeMillis();
        ClientRequestHistory history = clientHistory.computeIfAbsent(
            clientId,
            k -> new ClientRequestHistory()
        );
        return history.recordRequest(now);
    }
    
    private class ClientRequestHistory {
        private final Deque<Long> requestTimestamps = new ArrayDeque<>();
        
        synchronized boolean recordRequest(long now) {
            removeExpiredRequests(now);
            
            if (requestTimestamps.size() < maxRequests) {
                requestTimestamps.addLast(now);
                return true;
            }
            return false;
        }
        
        private void removeExpiredRequests(long now) {
            while (!requestTimestamps.isEmpty()) {
                long oldest = requestTimestamps.peekFirst();
                if (now - oldest >= windowDurationMillis) {
                    requestTimestamps.removeFirst();
                } else {
                    break;
                }
            }
        }
    }
}
