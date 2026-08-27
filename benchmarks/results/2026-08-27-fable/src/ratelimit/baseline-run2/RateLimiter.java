import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Sliding-window rate limiter, keyed per client. Thread-safe.
 */
public final class RateLimiter {

    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "rate-limiter-cleanup");
                t.setDaemon(true);
                return t;
            });

    public RateLimiter(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.windowMillis = window.toMillis();
        // Evict idle clients so the map cannot grow without bound.
        cleaner.scheduleAtFixedRate(this::evictIdleClients,
                windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * @return 0 if the request is allowed, otherwise the number of seconds
     *         the client should wait before retrying.
     */
    public long tryAcquire(String clientKey) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps =
                requestLog.computeIfAbsent(clientKey, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            long cutoff = now - windowMillis;
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= cutoff) {
                timestamps.pollFirst();
            }
            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                return 0;
            }
            long oldest = timestamps.peekFirst();
            long retryAfterMillis = (oldest + windowMillis) - now;
            return Math.max(1, (retryAfterMillis + 999) / 1000);
        }
    }

    private void evictIdleClients() {
        long cutoff = System.currentTimeMillis() - windowMillis;
        requestLog.entrySet().removeIf(entry -> {
            Deque<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                Long newest = timestamps.peekLast();
                return newest == null || newest <= cutoff;
            }
        });
    }
}
