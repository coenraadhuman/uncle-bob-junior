import java.io.Closeable;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

final class ClientRateLimiterRegistry implements Closeable {

    private static final Duration CLEANUP_INTERVAL = Duration.ofMinutes(5);
    private static final Duration IDLE_RETENTION = Duration.ofMinutes(10);

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Clock clock;
    private final ConcurrentHashMap<String, TokenBucket> bucketsByClient = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor;

    ClientRateLimiterRegistry(int maxRequestsPerWindow, Duration window, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
        this.clock = clock;
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(this::newDaemonThread);
        this.cleanupExecutor.scheduleAtFixedRate(
                this::evictIdleBuckets, CLEANUP_INTERVAL.toMillis(), CLEANUP_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
    }

    boolean tryAcquire(String clientKey) {
        return bucketsByClient
                .computeIfAbsent(clientKey, key -> new TokenBucket(maxRequestsPerWindow, window, clock))
                .tryConsume();
    }

    @Override
    public void close() {
        cleanupExecutor.shutdownNow();
    }

    private void evictIdleBuckets() {
        long now = clock.millis();
        bucketsByClient.entrySet().removeIf(entry -> entry.getValue().isIdleAsOf(now, IDLE_RETENTION.toMillis()));
    }

    private Thread newDaemonThread(Runnable task) {
        Thread thread = new Thread(task, "rate-limiter-cleanup");
        thread.setDaemon(true);
        return thread;
    }
}
