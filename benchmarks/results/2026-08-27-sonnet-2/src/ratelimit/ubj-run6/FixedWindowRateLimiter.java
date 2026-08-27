// FixedWindowRateLimiter.java
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

// ubj: unbounded map growth across distinct clientIds — add a scheduled
// eviction of expired windows if this runs long enough for that to matter.
public final class FixedWindowRateLimiter implements RateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration windowDuration;
    private final Clock clock;
    private final ConcurrentHashMap<String, Window> windowsByClient = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDuration = windowDuration;
        this.clock = clock;
    }

    @Override
    public boolean tryAcquire(String clientId) {
        long now = clock.millis();
        Window updated = windowsByClient.compute(clientId, (id, existing) -> nextWindow(existing, now));
        return updated.count() <= maxRequestsPerWindow;
    }

    private Window nextWindow(Window existing, long now) {
        if (existing == null || isExpired(existing, now)) {
            return new Window(now, 1);
        }
        return new Window(existing.startMillis(), existing.count() + 1);
    }

    private boolean isExpired(Window window, long now) {
        return now - window.startMillis() >= windowDuration.toMillis();
    }

    private record Window(long startMillis, int count) {}
}
