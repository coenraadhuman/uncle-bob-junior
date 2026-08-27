import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fixed-window rate limiter. Thread-safe; one window per client id.
 *
 * @implNote Uses {@link ConcurrentHashMap#compute} so the check-and-increment
 * for a given client is atomic without a separate lock.
 */
public final class RateLimiter {

    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final Clock clock;
    private final ConcurrentHashMap<String, ClientWindow> clientWindows = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequestsPerWindow, Duration window, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = window.toMillis();
        this.clock = clock;
    }

    /** @return true if the client is under its limit for the current window. */
    public boolean tryAcquire(String clientId) {
        long now = clock.millis();
        AtomicBoolean allowed = new AtomicBoolean();

        clientWindows.compute(clientId, (id, existing) -> {
            ClientWindow current = isExpired(existing, now) ? new ClientWindow(now, 0) : existing;
            allowed.set(current.count() < maxRequestsPerWindow);
            return allowed.get() ? current.incremented() : current;
        });

        return allowed.get();
    }

    /** Removes windows that expired more than {@code maxAge} ago, to bound memory use. */
    public void evictStaleClients(Duration maxAge) {
        long cutoff = clock.millis() - windowMillis - maxAge.toMillis();
        clientWindows.values().removeIf(window -> window.windowStartMillis() < cutoff);
    }

    private boolean isExpired(ClientWindow window, long now) {
        return window == null || now - window.windowStartMillis() >= windowMillis;
    }

    private record ClientWindow(long windowStartMillis, int count) {
        ClientWindow incremented() {
            return new ClientWindow(windowStartMillis, count + 1);
        }
    }
}
