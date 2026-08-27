import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Allows each client a fixed number of requests within a sliding time window.
 * Thread-safe. The injected Clock keeps time controllable in tests.
 */
final class SlidingWindowRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Clock clock;

    // ubj: entries are never evicted; swap for an expiring cache if client cardinality grows large.
    private final ConcurrentMap<String, Deque<Instant>> requestTimesByClient = new ConcurrentHashMap<>();

    SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration window, Clock clock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
        this.clock = clock;
    }

    /** Records a request for the client and reports whether it is within the limit. */
    boolean tryAcquire(String clientId) {
        Deque<Instant> requestTimes =
                requestTimesByClient.computeIfAbsent(clientId, id -> new ArrayDeque<>());
        synchronized (requestTimes) {
            pruneExpired(requestTimes);
            if (requestTimes.size() >= maxRequestsPerWindow) {
                return false;
            }
            requestTimes.addLast(clock.instant());
            return true;
        }
    }

    /** Seconds until the client's oldest recorded request leaves the window; 0 if not limited. */
    long retryAfterSeconds(String clientId) {
        Deque<Instant> requestTimes = requestTimesByClient.get(clientId);
        if (requestTimes == null) {
            return 0;
        }
        synchronized (requestTimes) {
            pruneExpired(requestTimes);
            if (requestTimes.size() < maxRequestsPerWindow) {
                return 0;
            }
            Instant oldestExpiry = requestTimes.peekFirst().plus(window);
            return Math.max(1, Duration.between(clock.instant(), oldestExpiry).toSeconds());
        }
    }

    private void pruneExpired(Deque<Instant> requestTimes) {
        Instant cutoff = clock.instant().minus(window);
        while (!requestTimes.isEmpty() && requestTimes.peekFirst().isBefore(cutoff)) {
            requestTimes.removeFirst();
        }
    }
}
