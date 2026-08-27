// File: FixedWindowRateLimiter.java
package com.example.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Limits each client to a fixed number of requests per time window.
 *
 * <p>Uses the fixed-window counter algorithm: time is divided into
 * consecutive windows of {@code windowDuration}; each client may make at
 * most {@code maxRequestsPerWindow} requests within a single window. A
 * client can burst up to twice that count across a window boundary; this
 * is accepted since the limit only needs to be approximate.
 */
public final class FixedWindowRateLimiter implements AutoCloseable {

    private final int maxRequestsPerWindow;
    private final long windowDurationMillis;
    private final Clock clock;
    private final Map<String, WindowState> requestWindows = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor;

    public FixedWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDurationMillis = windowDuration.toMillis();
        this.clock = clock;
        this.cleanupExecutor =
                Executors.newSingleThreadScheduledExecutor(FixedWindowRateLimiter::newDaemonCleanupThread);
        this.cleanupExecutor.scheduleAtFixedRate(
                this::evictStaleWindows, windowDurationMillis, windowDurationMillis, TimeUnit.MILLISECONDS);
    }

    /** Returns true if the client may proceed, false if it has exceeded the limit for the current window. */
    public boolean tryAcquire(String clientId) {
        long currentWindowIndex = currentWindowIndex();
        WindowState updated =
                requestWindows.compute(clientId, (id, existing) -> nextWindowState(existing, currentWindowIndex));
        return updated.requestCount() <= maxRequestsPerWindow;
    }

    @Override
    public void close() {
        cleanupExecutor.shutdownNow();
    }

    private long currentWindowIndex() {
        return clock.millis() / windowDurationMillis;
    }

    private static WindowState nextWindowState(WindowState existing, long currentWindowIndex) {
        if (existing == null || existing.windowIndex() != currentWindowIndex) {
            return new WindowState(currentWindowIndex, 1);
        }
        return new WindowState(currentWindowIndex, existing.requestCount() + 1);
    }

    private void evictStaleWindows() {
        long currentWindowIndex = currentWindowIndex();
        requestWindows.values().removeIf(window -> window.windowIndex() < currentWindowIndex);
    }

    private static Thread newDaemonCleanupThread(Runnable task) {
        Thread thread = new Thread(task, "rate-limiter-cleanup");
        thread.setDaemon(true);
        return thread;
    }

    private record WindowState(long windowIndex, int requestCount) {}
}
