import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final long WINDOW_MILLIS = 60_000;
    private static final int MAX_REQUESTS = 10;
    
    private final Map<String, ClientWindow> windows = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanup = 
        Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "RateLimiter-Cleanup");
            t.setDaemon(true);
            return t;
        });
    
    public RateLimiter() {
        cleanup.scheduleAtFixedRate(this::purgeExpired, 1, 1, TimeUnit.MINUTES);
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        ClientWindow window = windows.computeIfAbsent(clientId, k -> new ClientWindow(now));
        return window.allowRequest(now);
    }
    
    private void purgeExpired() {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(e -> now - e.getValue().lastAccessTime() > WINDOW_MILLIS * 2);
    }
    
    public void shutdown() {
        cleanup.shutdown();
    }
    
    private static class ClientWindow {
        private long windowStart;
        private int requestCount;
        private long lastAccess;
        
        ClientWindow(long startTime) {
            this.windowStart = startTime;
            this.requestCount = 0;
            this.lastAccess = startTime;
        }
        
        synchronized boolean allowRequest(long now) {
            lastAccess = now;
            
            if (now - windowStart >= WINDOW_MILLIS) {
                windowStart = now;
                requestCount = 0;
            }
            
            if (requestCount < MAX_REQUESTS) {
                requestCount++;
                return true;
            }
            return false;
        }
        
        synchronized long lastAccessTime() {
            return lastAccess;
        }
    }
}
