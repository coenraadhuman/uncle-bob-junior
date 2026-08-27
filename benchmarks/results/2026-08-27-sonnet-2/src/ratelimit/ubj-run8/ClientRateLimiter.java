import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lock-free, fixed-window rate limiter keyed by client id.
 * Thread-safe: safe to share one instance across all requests.
 */
public final class ClientRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration windowDuration;
    private final Clock clock;
    private final ConcurrentHashMap<String, AtomicReference<Window>> windowsByClient =
            new ConcurrentHashMap<>();

    public ClientRateLimiter(int maxRequestsPerWindow, Duration windowDuration, Clock clock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDuration = Objects.requireNonNull(windowDuration);
        this.clock = Objects.requireNonNull(clock);
    }

    /** Returns true if the request is allowed, false if the client exceeded its quota. */
    public boolean tryAcquire(String clientId) {
        Instant now = clock.instant();
        AtomicReference<Window> slot =
                windowsByClient.computeIfAbsent(clientId, id -> new AtomicReference<>(Window.startingAt(now)));

        while (true) {
            Window current = slot.get();
            Window effective = current.isExpired(now, windowDuration) ? Window.startingAt(now) : current;

            if (effective.requestCount() >= maxRequestsPerWindow) {
                slot.compareAndSet(current, effective);
                return false;
            }
            if (slot.compareAndSet(current, effective.incremented())) {
                return true;
            }
        }
    }

    private record Window(Instant windowStart, int requestCount) {

        static Window startingAt(Instant start) {
            return new Window(start, 0);
        }

        boolean isExpired(Instant now, Duration windowDuration) {
            return Duration.between(windowStart, now).compareTo(windowDuration) >= 0;
        }

        Window incremented() {
            return new Window(windowStart, requestCount + 1);
        }
    }
}
