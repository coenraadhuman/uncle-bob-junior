// SlidingWindowRateLimiter.java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class SlidingWindowRateLimiter implements RateLimiter, AutoCloseable {

    private final int maxRequestsPerWindow;
    private final Duration windowDuration;
    private final Clock clock;
    private final ConcurrentMap<String, Deque<Instant>> timestampsByClient = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(
            runnable -> {
                Thread thread = new Thread(runnable, "rate-limiter-cleanup");
                thread.setDaemon(true);
                return thread;
            });

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration) {
        this(maxRequestsPerWindow, windowDuration, Clock.systemUTC());
    }

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDuration = windowDuration;
        this.clock = clock;
        long periodMillis = windowDuration.toMillis();
        cleanupExecutor.scheduleAtFixedRate(this::evictStaleClients, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean tryAcquire(String clientId) {
        Deque<Instant> timestamps = timestampsByClient.computeIfAbsent(clientId, id -> new ArrayDeque<>());
        synchronized (timestamps) {
            Instant now = clock.instant();
            evictExpired(timestamps, now);
            if (timestamps.size() >= maxRequestsPerWindow) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    private void evictExpired(Deque<Instant> timestamps, Instant now) {
        Instant windowStart = now.minus(windowDuration);
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
            timestamps.removeFirst();
        }
    }

    private void evictStaleClients() {
        Instant now = clock.instant();
        timestampsByClient.forEach((clientId, timestamps) -> {
            synchronized (timestamps) {
                evictExpired(timestamps, now);
                if (timestamps.isEmpty()) {
                    timestampsByClient.remove(clientId, timestamps);
                }
            }
        });
    }

    @Override
    public void close() {
        cleanupExecutor.shutdown();
    }
}
