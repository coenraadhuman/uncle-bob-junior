import java.util.*;
import java.util.concurrent.*;

public class ClientRateLimiter {
    private static final int MAX_REQUESTS_PER_MINUTE = 20;
    private static final long WINDOW_SIZE_MS = 60_000;
    
    private final Map<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public ClientRateLimiter() {
        scheduler.scheduleAtFixedRate(this::cleanup, 5, 5, TimeUnit.MINUTES);
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = requestTimestamps.computeIfAbsent(clientId, k -> new ConcurrentLinkedDeque<>());
        
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - WINDOW_SIZE_MS) {
            timestamps.removeFirst();
        }
        
        if (timestamps.size() < MAX_REQUESTS_PER_MINUTE) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }
    
    private void cleanup() {
        long now = System.currentTimeMillis();
        requestTimestamps.entrySet().removeIf(entry -> 
            entry.getValue().isEmpty() || entry.getValue().peekLast() < now - WINDOW_SIZE_MS
        );
    }
    
    public void shutdown() {
        scheduler.shutdown();
    }
}
