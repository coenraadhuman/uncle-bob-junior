import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Sliding-window rate limiter keyed by client id (e.g. IP address).
 * Allows at most {@code maxRequests} per {@code window} per client.
 */
public final class RateLimiter {

    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.windowMillis = window.toMillis();
    }

    /** Returns true if the request is allowed, false if the client is over the limit. */
    public boolean tryAcquire(String clientId) {
        long now = System.currentTimeMillis();
        Window w = windows.compute(clientId, (id, existing) -> {
            if (existing == null || now - existing.startMillis >= windowMillis) {
                return new Window(now);
            }
            return existing;
        });
        return w.count.incrementAndGet() <= maxRequests;
    }

    /** Seconds until this client's current window resets (for Retry-After). */
    public long secondsUntilReset(String clientId) {
        Window w = windows.get(clientId);
        if (w == null) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - w.startMillis;
        return Math.max(0, (windowMillis - elapsed + 999) / 1000);
    }

    /** Drop expired windows so the map doesn't grow unboundedly. Call periodically. */
    public void evictExpired() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Window>> it = windows.entrySet().iterator();
        while (it.hasNext()) {
            if (now - it.next().getValue().startMillis >= windowMillis) {
                it.remove();
            }
        }
    }

    private static final class Window {
        final long startMillis;
        final AtomicLong count = new AtomicLong();

        Window(long startMillis) {
            this.startMillis = startMillis;
        }
    }
}
