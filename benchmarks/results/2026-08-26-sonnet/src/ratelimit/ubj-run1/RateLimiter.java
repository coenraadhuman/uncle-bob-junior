package com.example.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Sliding-window request counter, one window per client key.
 */
public final class RateLimiter implements AutoCloseable {

    private final int maxRequestsPerWindow;
    private final Duration windowDuration;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Instant>> requestTimestampsByClient = new ConcurrentHashMap<>();
    private final ScheduledExecutorService staleClientCleaner = Executors.newSingleThreadScheduledExecutor(this::newDaemonThread);

    public RateLimiter(int maxRequestsPerWindow, Duration windowDuration) {
        this(maxRequestsPerWindow, windowDuration, Clock.systemUTC());
    }

    public RateLimiter(int maxRequestsPerWindow, Duration windowDuration, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDuration = windowDuration;
        this.clock = clock;
        long cleanupIntervalSeconds = Math.max(1, windowDuration.toSeconds());
        staleClientCleaner.scheduleAtFixedRate(
                this::removeClientsWithNoRecentRequests, cleanupIntervalSeconds, cleanupIntervalSeconds, TimeUnit.SECONDS);
    }

    public boolean isRequestAllowed(String clientKey) {
        Deque<Instant> timestamps = requestTimestampsByClient.computeIfAbsent(clientKey, key -> new ConcurrentLinkedDeque<>());
        synchronized (timestamps) {
            evictExpiredTimestamps(timestamps);
            if (timestamps.size() >= maxRequestsPerWindow) {
                return false;
            }
            timestamps.addLast(clock.instant());
            return true;
        }
    }

    public Duration windowDuration() {
        return windowDuration;
    }

    @Override
    public void close() {
        staleClientCleaner.shutdownNow();
    }

    private void evictExpiredTimestamps(Deque<Instant> timestamps) {
        Instant windowStart = clock.instant().minus(windowDuration);
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
            timestamps.pollFirst();
        }
    }

    private void removeClientsWithNoRecentRequests() {
        requestTimestampsByClient.forEach((clientKey, timestamps) -> {
            synchronized (timestamps) {
                evictExpiredTimestamps(timestamps);
                if (timestamps.isEmpty()) {
                    requestTimestampsByClient.remove(clientKey, timestamps);
                }
            }
        });
    }

    private Thread newDaemonThread(Runnable task) {
        Thread thread = new Thread(task, "rate-limiter-cleanup");
        thread.setDaemon(true);
        return thread;
    }
}
