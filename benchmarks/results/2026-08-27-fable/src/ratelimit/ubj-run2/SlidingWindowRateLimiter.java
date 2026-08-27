import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Sliding-window rate limiter: each client may make at most
 * {@code maxRequests} requests within any {@code window}-long period.
 *
 * <p>Thread-safe. State is in-memory, so limits apply per process,
 * not across a cluster.
 */
public final class SlidingWindowRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Clock clock;
    private final ConcurrentMap<String, Deque<Long>> requestTimesByClient =
            new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration window, Clock clock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
        this.clock = clock;
    }

    /**
     * Records a request for {@code clientId} if the client is under its limit.
     *
     * @return true if the request is allowed, false if the client must wait
     */
    public boolean tryAcquire(String clientId) {
        long now = clock.millis();
        Deque<Long> requestTimes =
                requestTimesByClient.computeIfAbsent(clientId, id -> new ArrayDeque<>());
        synchronized (requestTimes) {
            evictExpired(requestTimes, now);
            if (requestTimes.size() >= maxRequestsPerWindow) {
                return false;
            }
            requestTimes.addLast(now);
            return true;
        }
    }

    /** Seconds until {@code clientId} may retry; zero if it may retry now. */
    public long secondsUntilRetry(String clientId) {
        Deque<Long> requestTimes = requestTimesByClient.get(clientId);
        if (requestTimes == null) {
            return 0;
        }
        synchronized (requestTimes) {
            evictExpired(requestTimes, clock.millis());
            if (requestTimes.size() < maxRequestsPerWindow) {
                return 0;
            }
            long oldestExpiresAt = requestTimes.peekFirst() + window.toMillis();
            long millisUntilRetry = Math.max(0, oldestExpiresAt - clock.millis());
            return (millisUntilRetry + 999) / 1000; // round up to whole seconds
        }
    }

    private void evictExpired(Deque<Long> requestTimes, long now) {
        long windowStart = now - window.toMillis();
        while (!requestTimes.isEmpty() && requestTimes.peekFirst() <= windowStart) {
            requestTimes.removeFirst();
        }
    }
}
