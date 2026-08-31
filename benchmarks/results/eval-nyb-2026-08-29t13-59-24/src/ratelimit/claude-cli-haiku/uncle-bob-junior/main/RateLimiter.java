import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final long ONE_MINUTE_MILLIS = 60_000;
    
    private final int maxRequestsPerMinute;
    private final Map<String, ClientRequestState> clientStates;
    
    private static class ClientRequestState {
        long windowStart;
        int requestCount;
        
        ClientRequestState(long now) {
            this.windowStart = now;
            this.requestCount = 0;
        }
    }
    
    public RateLimiter(int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.clientStates = new ConcurrentHashMap<>();
    }
    
    public synchronized boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        
        ClientRequestState state = clientStates.computeIfAbsent(clientId, id -> new ClientRequestState(now));
        
        if (now - state.windowStart > ONE_MINUTE_MILLIS) {
            state.windowStart = now;
            state.requestCount = 0;
        }
        
        if (state.requestCount >= maxRequestsPerMinute) {
            return false;
        }
        
        state.requestCount++;
        return true;
    }
}
