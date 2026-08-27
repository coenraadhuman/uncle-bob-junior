package ratelimit;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding-window rate limiter: each client may make at most
 * {@code maxRequestsPerWindow} requests within any rolling {@code window}.
 *
 * Thread-safe. State is in-memory, so limits apply per server instance.
 * Time is injected by the caller, which keeps this class a pure function
 * of its inputs and makes it testable without sleeping.
 */
public final class SlidingWindowRateLimiter {

    /** Outcome of one acquire attempt; {@code retryAfterSeconds} is 0 when allowed. */
    public record Decision(boolean allowed, long retryAfterSeconds) {}

    private static final long MILLIS_PER_SECOND = 1000;

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Map<String, Deque<Instant>> requestTimesByClient = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration window) {
        if (maxRequestsPerWindow < 1) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be >= 1, was " + maxRequestsPerWindow);
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
    }

    /** Records the request if the client is within its limit, and says whether it was. */
    public Decision tryAcquire(String clientId, Instant now) {
        // compute() makes evict-decide-append atomic per client; the one-element
        // array carries the lambda's decision out to the caller.
        Decision[] decision = new Decision[1];
        requestTimesByClient.compute(clientId, (id, times) -> {
            Deque<Instant> recent = times == null ? new ArrayDeque<>() : times;
            evictOutsideWindow(recent, now);
            decision[0] = decide(recent, now);
            if (decision[0].allowed()) {
                recent.addLast(now);
            }
            return recent.isEmpty() ? null : recent; // drop idle clients so the map cannot grow unbounded
        });
        return decision[0];
    }

    private Decision decide(Deque<Instant> recentRequests, Instant now) {
        if (recentRequests.size() < maxRequestsPerWindow) {
            return new Decision(true, 0);
        }
        Instant oldestLeavesWindowAt = recentRequests.peekFirst().plus(window);
        return new Decision(false, ceilSeconds(Duration.between(now, oldestLeavesWindowAt)));
    }

    private void evictOutsideWindow(Deque<Instant> recentRequests, Instant now) {
        Instant windowStart = now.minus(window);
        while (!recentRequests.isEmpty() && !recentRequests.peekFirst().isAfter(windowStart)) {
            recentRequests.removeFirst();
        }
    }

    /** Rounds up so a client that waits exactly Retry-After seconds is admitted. */
    private static long ceilSeconds(Duration duration) {
        return Math.max(1, (duration.toMillis() + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND);
    }
}
