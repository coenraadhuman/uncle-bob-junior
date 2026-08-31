package ratelimit;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/**
 * Fixed-window rate limiter. Each client id gets its own window of
 * {@code windowDuration}; once {@code maxRequestsPerWindow} requests have
 * been recorded, further calls to {@link #tryAcquire(String)} return false
 * until the window rolls over.
 */
public final class RateLimiter {

    private static final int INITIAL_REQUEST_COUNT = 1;

    private final int maxRequestsPerWindow;
    private final long windowDurationMillis;
    private final LongSupplier currentTimeMillis;

    // ubj: unbounded map, one entry per distinct client id forever; add eviction
    // (e.g. Caffeine with expireAfterAccess) if this runs long enough to see
    // many unique clients.
    private final ConcurrentMap<String, AtomicReference<WindowState>> clientWindows =
            new ConcurrentHashMap<>();

    public RateLimiter(int maxRequestsPerWindow, Duration windowDuration) {
        this(maxRequestsPerWindow, windowDuration, System::currentTimeMillis);
    }

    RateLimiter(int maxRequestsPerWindow, Duration windowDuration, LongSupplier currentTimeMillis) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDurationMillis = windowDuration.toMillis();
        this.currentTimeMillis = currentTimeMillis;
    }

    /** Returns true if the client may proceed, false if it has exceeded its limit for the current window. */
    public boolean tryAcquire(String clientId) {
        AtomicReference<WindowState> stateRef =
                clientWindows.computeIfAbsent(clientId, id -> new AtomicReference<>());
        long now = currentTimeMillis.getAsLong();
        WindowState current;
        WindowState next;
        do {
            current = stateRef.get();
            next = nextState(current, now);
            if (next == null) {
                return false;
            }
        } while (!stateRef.compareAndSet(current, next));
        return true;
    }

    private WindowState nextState(WindowState current, long now) {
        if (current == null || isWindowExpired(current, now)) {
            return new WindowState(now, INITIAL_REQUEST_COUNT);
        }
        if (current.requestCount() >= maxRequestsPerWindow) {
            return null;
        }
        return new WindowState(current.windowStartMillis(), current.requestCount() + 1);
    }

    private boolean isWindowExpired(WindowState state, long now) {
        return now - state.windowStartMillis() >= windowDurationMillis;
    }
}
