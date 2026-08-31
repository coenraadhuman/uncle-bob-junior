import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Sliding-window rate limiter: at most {@code maxRequestsPerWindow} calls
 * per client within any rolling {@code window}. Thread-safe.
 */
public final class SlidingWindowRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Clock clock;
    // ubj: idle-client entries are never evicted; add scheduled cleanup if client cardinality grows large
    private final ConcurrentMap<String, Deque<Instant>> requestTimesByClient = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration window, Clock clock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
        this.clock = clock;
    }

    /** Returns true and records the request if the client is under its limit. */
    public boolean tryAcquire(String clientId) {
        Deque<Instant> requestTimes =
                requestTimesByClient.computeIfAbsent(clientId, id -> new ArrayDeque<>());
        synchronized (requestTimes) {
            evictOlderThanWindow(requestTimes);
            if (requestTimes.size() >= maxRequestsPerWindow) {
                return false;
            }
            requestTimes.addLast(clock.instant());
            return true;
        }
    }

    private void evictOlderThanWindow(Deque<Instant> requestTimes) {
        Instant windowStart = clock.instant().minus(window);
        while (!requestTimes.isEmpty() && requestTimes.peekFirst().isBefore(windowStart)) {
            requestTimes.removeFirst();
        }
    }

    long windowSeconds() {
        return window.toSeconds();
    }
}
