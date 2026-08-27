package ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sliding-window rate limiter: each client may make at most
 * {@code maxRequestsPerWindow} requests within any rolling {@code window}.
 * Thread-safe: per-client state is only touched inside the map's atomic compute.
 */
public final class SlidingWindowRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Instant>> requestTimesByClient =
            new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration window, Clock clock) {
        if (maxRequestsPerWindow < 1) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be at least 1");
        }
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** Records the request if the client is under its limit and reports the decision. */
    public RateLimitDecision check(String clientId) {
        AtomicReference<RateLimitDecision> decision = new AtomicReference<>();
        requestTimesByClient.compute(clientId, (id, existingTimes) -> {
            Deque<Instant> times = existingTimes == null ? new ArrayDeque<>() : existingTimes;
            decision.set(decide(times));
            return times;
        });
        return decision.get();
    }

    /** Drops state for clients whose requests have all left the window. Call periodically. */
    public void purgeIdleClients() {
        Instant cutoff = clock.instant().minus(window);
        for (String clientId : requestTimesByClient.keySet()) {
            requestTimesByClient.computeIfPresent(clientId, (id, times) -> {
                evictOlderThan(times, cutoff);
                return times.isEmpty() ? null : times;
            });
        }
    }

    private RateLimitDecision decide(Deque<Instant> times) {
        Instant now = clock.instant();
        evictOlderThan(times, now.minus(window));
        if (times.size() >= maxRequestsPerWindow) {
            return RateLimitDecision.rejectedFor(timeUntilOldestExpires(times, now));
        }
        times.addLast(now);
        return RateLimitDecision.allowed();
    }

    private Duration timeUntilOldestExpires(Deque<Instant> times, Instant now) {
        return Duration.between(now, times.peekFirst().plus(window));
    }

    private static void evictOlderThan(Deque<Instant> times, Instant cutoff) {
        while (!times.isEmpty() && !times.peekFirst().isAfter(cutoff)) {
            times.removeFirst();
        }
    }

    int trackedClientCount() {
        // ubj: package-private, exists only so tests can verify purge frees memory
        return requestTimesByClient.size();
    }
}
