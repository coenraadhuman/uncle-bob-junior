// FixedWindowRateLimiter.java
package ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Fixed-window per-client rate limiter.
 * Each client gets at most {@code maxRequestsPerWindow} requests per {@code windowDuration}.
 * ubj: fixed windows allow a short burst around window boundaries (up to 2x the limit);
 * switch to a sliding-window log if that becomes a real problem.
 */
public final class FixedWindowRateLimiter implements AutoCloseable {

    private final Map<String, AtomicReference<WindowState>> clientWindows = new ConcurrentHashMap<>();
    private final Duration windowDuration;
    private final int maxRequestsPerWindow;
    private final Clock clock;
    private final ScheduledExecutorService evictionExecutor;

    public FixedWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration) {
        this(maxRequestsPerWindow, windowDuration, Clock.systemUTC());
    }

    FixedWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDuration = windowDuration;
        this.clock = clock;
        this.evictionExecutor = newDaemonScheduler();
        long periodSeconds = windowDuration.getSeconds();
        evictionExecutor.scheduleAtFixedRate(
                this::evictInactiveEntries, periodSeconds, periodSeconds, TimeUnit.SECONDS);
    }

    public boolean tryAcquire(String clientId) {
        AtomicReference<WindowState> stateRef =
                clientWindows.computeIfAbsent(clientId, id -> new AtomicReference<>(freshWindowState()));
        while (true) {
            WindowState currentState = stateRef.get();
            WindowState nextState = advance(currentState);
            if (nextState.requestCount() > maxRequestsPerWindow) {
                return false;
            }
            if (stateRef.compareAndSet(currentState, nextState)) {
                return true;
            }
        }
    }

    public long windowDurationSeconds() {
        return windowDuration.getSeconds();
    }

    @Override
    public void close() {
        evictionExecutor.shutdownNow();
    }

    private WindowState advance(WindowState state) {
        long windowStart = currentWindowStart();
        return windowStart == state.windowStartSeconds()
                ? new WindowState(windowStart, state.requestCount() + 1)
                : new WindowState(windowStart, 1);
    }

    private WindowState freshWindowState() {
        return new WindowState(currentWindowStart(), 0);
    }

    private long currentWindowStart() {
        long windowSeconds = windowDuration.getSeconds();
        return clock.instant().getEpochSecond() / windowSeconds * windowSeconds;
    }

    private void evictInactiveEntries() {
        long activeWindowStart = currentWindowStart();
        clientWindows.entrySet().removeIf(entry -> entry.getValue().get().windowStartSeconds() != activeWindowStart);
    }

    private static ScheduledExecutorService newDaemonScheduler() {
        return Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "rate-limiter-eviction");
            thread.setDaemon(true);
            return thread;
        });
    }
}
