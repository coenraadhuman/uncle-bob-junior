package ratelimit;

import java.io.Closeable;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

/**
 * Allows at most {@code maxRequestsPerWindow} requests per client within each
 * fixed {@code windowDuration}. A background task evicts clients that have been
 * idle for a couple of windows, so memory does not grow unbounded.
 */
public final class FixedWindowRateLimiter implements RateLimiter, Closeable {

    private static final int EVICTION_GRACE_WINDOWS = 2;

    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final LongSupplier clock;
    private final ConcurrentHashMap<String, Window> windowsByClient = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor;

    public FixedWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration) {
        this(maxRequestsPerWindow, windowDuration, System::currentTimeMillis, true);
    }

    // Package-private: lets tests inject a fake clock and skip the real cleanup thread.
    FixedWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration, LongSupplier clock, boolean scheduleCleanup) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowDuration.toMillis();
        this.clock = clock;
        this.cleanupExecutor = scheduleCleanup ? startCleanupTask() : null;
    }

    @Override
    public boolean tryAcquire(String clientId) {
        long now = clock.getAsLong();
        Window window = windowsByClient.compute(clientId, (id, existing) -> currentOrNewWindow(existing, now));
        return window.count.incrementAndGet() <= maxRequestsPerWindow;
    }

    @Override
    public void close() {
        if (cleanupExecutor != null) {
            cleanupExecutor.shutdownNow();
        }
    }

    private Window currentOrNewWindow(Window existing, long now) {
        boolean expired = existing == null || now - existing.startMillis >= windowMillis;
        return expired ? new Window(now) : existing;
    }

    private ScheduledExecutorService startCleanupTask() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "rate-limiter-cleanup");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleAtFixedRate(this::evictStaleClients, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
        return executor;
    }

    private void evictStaleClients() {
        long staleThreshold = windowMillis * EVICTION_GRACE_WINDOWS;
        long now = clock.getAsLong();
        windowsByClient.entrySet().removeIf(entry -> now - entry.getValue().startMillis >= staleThreshold);
    }

    private static final class Window {
        final long startMillis;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long startMillis) {
            this.startMillis = startMillis;
        }
    }
}
