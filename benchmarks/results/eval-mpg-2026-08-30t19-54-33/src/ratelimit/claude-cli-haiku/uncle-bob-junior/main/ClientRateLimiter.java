import java.util.*;
import java.util.concurrent.*;

public class ClientRateLimiter {
    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, Deque<Long>> requestTimestamps;
    private final ScheduledExecutorService cleanupExecutor;
    
    public ClientRateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.requestTimestamps = new ConcurrentHashMap<>();
        this.cleanupExecutor = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "RateLimiter-Cleanup");
            t.setDaemon(true);
            return t;
        });
        scheduleCleanup();
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = requestTimestamps.computeIfAbsent(clientId, k -> new ConcurrentLinkedDeque<>());
        
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - windowMillis) {
            timestamps.removeFirst();
        }
        
        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }
    
    private void scheduleCleanup() {
        cleanupExecutor.scheduleAtFixedRate(
            this::removeInactiveClients,
            windowMillis,
            windowMillis,
            TimeUnit.MILLISECONDS
        );
    }
    
    private void removeInactiveClients() {
        long now = System.currentTimeMillis();
        requestTimestamps.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(timestamp -> timestamp < now - windowMillis);
            return entry.getValue().isEmpty();
        });
    }
    
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}
