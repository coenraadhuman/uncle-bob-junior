import java.util.concurrent.*;
import java.util.*;

public class RateLimiter {
    private static class ClientRateInfo {
        private final Queue<Long> requestTimes = new ConcurrentLinkedQueue<>();
        private final int maxRequests;
        private final long windowMillis;

        ClientRateInfo(int maxRequests, long windowMillis) {
            this.maxRequests = maxRequests;
            this.windowMillis = windowMillis;
        }

        boolean allowRequest() {
            long now = System.currentTimeMillis();
            
            // Remove timestamps outside the window
            requestTimes.removeIf(time -> now - time > windowMillis);
            
            if (requestTimes.size() < maxRequests) {
                requestTimes.offer(now);
                return true;
            }
            return false;
        }
    }

    private final ConcurrentHashMap<String, ClientRateInfo> clientLimits;
    private final int maxRequests;
    private final long windowMillis;
    private final ScheduledExecutorService cleaner;

    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.clientLimits = new ConcurrentHashMap<>();
        
        // Clean up inactive clients every 5 minutes
        this.cleaner = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "RateLimiter-Cleanup");
            t.setDaemon(true);
            return t;
        });
        
        cleaner.scheduleAtFixedRate(this::cleanupInactiveClients, 
            5, 5, TimeUnit.MINUTES);
    }

    public boolean allowRequest(String clientId) {
        return clientLimits
            .computeIfAbsent(clientId, k -> new ClientRateInfo(maxRequests, windowMillis))
            .allowRequest();
    }

    private void cleanupInactiveClients() {
        long now = System.currentTimeMillis();
        clientLimits.entrySet().removeIf(entry -> {
            ClientRateInfo info = entry.getValue();
            info.requestTimes.removeIf(time -> now - time > windowMillis);
            return info.requestTimes.isEmpty();
        });
    }

    public void shutdown() {
        cleaner.shutdown();
        try {
            if (!cleaner.awaitTermination(10, TimeUnit.SECONDS)) {
                cleaner.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleaner.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
