import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding-window rate limiter: each client may make at most
 * maxRequestsPerWindow requests within any rolling window.
 * Thread-safe; the Clock is injectable so tests control time.
 */
public final class SlidingWindowRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Instant>> requestTimesByClient =
            new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration window, Clock clock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
        this.clock = clock;
    }

    /** Returns true and records the request if the client is under its limit. */
    public boolean tryAcquire(String clientId) {
        while (true) {
            Deque<Instant> times =
                    requestTimesByClient.computeIfAbsent(clientId, id -> new ArrayDeque<>());
            synchronized (times) {
                // ubj: re-check mapping because removeIdleClients may have evicted
                // this deque between computeIfAbsent and taking the lock.
                if (requestTimesByClient.get(clientId) != times) {
                    continue;
                }
                evictExpired(times);
                if (times.size() >= maxRequestsPerWindow) {
                    return false;
                }
                times.addLast(clock.instant());
                return true;
            }
        }
    }

    /** Seconds until the client's oldest request leaves the window; 0 if not limited. */
    public long secondsUntilNextSlot(String clientId) {
        Deque<Instant> times = requestTimesByClient.get(clientId);
        if (times == null) {
            return 0;
        }
        synchronized (times) {
            evictExpired(times);
            if (times.size() < maxRequestsPerWindow) {
                return 0;
            }
            Instant oldestExpiry = times.peekFirst().plus(window);
            return Math.max(1, Duration.between(clock.instant(), oldestExpiry).getSeconds());
        }
    }

    /** Drops clients with no requests left in the window; call periodically to bound memory. */
    public void removeIdleClients() {
        requestTimesByClient.forEach((clientId, times) -> {
            synchronized (times) {
                evictExpired(times);
                if (times.isEmpty()) {
                    requestTimesByClient.remove(clientId, times);
                }
            }
        });
    }

    private void evictExpired(Deque<Instant> times) {
        Instant windowStart = clock.instant().minus(window);
        while (!times.isEmpty() && times.peekFirst().isBefore(windowStart)) {
            times.removeFirst();
        }
    }
}
