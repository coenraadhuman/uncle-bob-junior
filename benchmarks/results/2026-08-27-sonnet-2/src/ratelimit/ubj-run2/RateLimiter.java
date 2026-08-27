// RateLimiter.java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Fixed-window rate limiter. Thread-safe: each client's window is updated
 * via lock-free compare-and-swap, so concurrent requests for the same
 * client never double-count.
 */
public final class RateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration windowSize;
    private final Clock clock;
    // ubj: unbounded map, one entry per distinct client forever; add eviction
    // of stale windows if this runs long enough to see many unique clients.
    private final Map<String, AtomicReference<Window>> windowsByClient = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequestsPerWindow, Duration windowSize, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowSize = windowSize;
        this.clock = clock;
    }

    public boolean tryAcquire(String clientId) {
        AtomicReference<Window> slot = windowsByClient.computeIfAbsent(
                clientId, id -> new AtomicReference<>(new Window(clock.instant(), 0)));

        while (true) {
            Window current = slot.get();
            Window next = nextWindow(current);

            if (next.count() > maxRequestsPerWindow) {
                return false;
            }
            if (slot.compareAndSet(current, next)) {
                return true;
            }
        }
    }

    private Window nextWindow(Window current) {
        if (isExpired(current)) {
            return new Window(clock.instant(), 1);
        }
        return new Window(current.start(), current.count() + 1);
    }

    private boolean isExpired(Window window) {
        return clock.instant().isAfter(window.start().plus(windowSize));
    }

    private record Window(Instant start, int count) {
    }
}
