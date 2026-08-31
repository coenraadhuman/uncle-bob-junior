import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding-window rate limiter, keyed by client identifier.
 * Thread-safe and self-cleaning: idle clients are evicted lazily.
 */
public final class RateLimiter {

    private final int maxRequests;
    private final long windowNanos;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.windowNanos = window.toNanos();
    }

    /** Returns true if the request is allowed, false if the client is over the limit. */
    public boolean tryAcquire(String clientId) {
        long now = System.nanoTime();
        Window w = windows.computeIfAbsent(clientId, k -> new Window(maxRequests));
        boolean allowed = w.tryAcquire(now, windowNanos);
        // Occasionally sweep out entries for clients that have gone quiet,
        // so the map does not grow without bound.
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(e -> e.getValue().isIdle(now, windowNanos));
        }
        return allowed;
    }

    /** Seconds until the oldest tracked request leaves the window; used for Retry-After. */
    public long retryAfterSeconds(String clientId) {
        Window w = windows.get(clientId);
        if (w == null) {
            return 0;
        }
        return w.retryAfterSeconds(System.nanoTime(), windowNanos);
    }

    /** Ring buffer of the timestamps of the last maxRequests requests. */
    private static final class Window {
        private final long[] timestamps;
        private int index;
        private int count;

        Window(int capacity) {
            this.timestamps = new long[capacity];
        }

        synchronized boolean tryAcquire(long now, long windowNanos) {
            if (count == timestamps.length) {
                long oldest = timestamps[index];
                if (now - oldest < windowNanos) {
                    return false;
                }
                // Oldest request has aged out; overwrite it.
                timestamps[index] = now;
                index = (index + 1) % timestamps.length;
                return true;
            }
            timestamps[(index + count) % timestamps.length] = now;
            count++;
            return true;
        }

        synchronized long retryAfterSeconds(long now, long windowNanos) {
            if (count < timestamps.length) {
                return 0;
            }
            long oldest = timestamps[index];
            long remaining = windowNanos - (now - oldest);
            return remaining <= 0 ? 0 : Math.max(1, remaining / 1_000_000_000L);
        }

        synchronized boolean isIdle(long now, long windowNanos) {
            if (count == 0) {
                return true;
            }
            int newest = (index + count - 1) % timestamps.length;
            return now - timestamps[newest] > windowNanos;
        }
    }
}
