import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimiter {
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MILLIS = TimeUnit.MINUTES.toMillis(1);
    private final ConcurrentHashMap<String, RequestWindow> windows = new ConcurrentHashMap<>();

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        RequestWindow window = windows.compute(clientId, (key, existing) -> {
            if (existing == null || now - existing.startTime >= WINDOW_MILLIS) {
                return new RequestWindow(now, 1);
            }
            existing.increment();
            return existing;
        });
        
        boolean allowed = window.count <= MAX_REQUESTS;
        if (allowed) {
            cleanupOldWindows(now);
        }
        return allowed;
    }

    private void cleanupOldWindows(long now) {
        windows.entrySet().removeIf(entry ->
            now - entry.getValue().startTime >= WINDOW_MILLIS
        );
    }

    private static class RequestWindow {
        final long startTime;
        int count;

        RequestWindow(long startTime, int count) {
            this.startTime = startTime;
            this.count = count;
        }

        void increment() {
            count++;
        }
    }
}
