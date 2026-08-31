import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_MS = 60_000;
    
    private final ConcurrentHashMap<String, Deque<Long>> clientRequests = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newScheduledThreadPool(1);
    
    public RateLimiter() {
        cleaner.scheduleAtFixedRate(this::cleanupExpired, 1, 1, TimeUnit.MINUTES);
    }
    
    public boolean allowRequest(String clientIp) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequests.computeIfAbsent(clientIp, k -> new ConcurrentLinkedDeque<>());
        
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                timestamps.removeFirst();
            }
            
            if (timestamps.size() < REQUESTS_PER_MINUTE) {
                timestamps.addLast(now);
                return true;
            }
            return false;
        }
    }
    
    private void cleanupExpired() {
        long threshold = System.currentTimeMillis() - WINDOW_MS;
        clientRequests.forEach((ip, timestamps) -> {
            synchronized (timestamps) {
                timestamps.removeIf(ts -> ts < threshold);
            }
        });
        clientRequests.values().removeIf(Deque::isEmpty);
    }
    
    public void shutdown() {
        cleaner.shutdown();
    }
}
