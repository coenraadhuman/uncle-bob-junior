import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wraps any HttpHandler with per-client rate limiting using a sliding
 * window: a client may make at most {@code maxRequests} requests in any
 * rolling {@code windowMillis} period. Excess requests get HTTP 429.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;

    /** Per-client timestamps of recent requests (millis since epoch). */
    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleaner =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "rate-limiter-cleanup");
                t.setDaemon(true);
                return t;
            });

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        // Evict clients that have gone quiet so the map doesn't grow forever.
        cleaner.scheduleAtFixedRate(this::evictStaleClients,
                windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientId(exchange);
        long now = System.currentTimeMillis();

        long retryAfterSeconds = tryAcquire(clientId, now);
        if (retryAfterSeconds > 0) {
            reject(exchange, retryAfterSeconds);
            return;
        }
        delegate.handle(exchange);
    }

    /**
     * Records the request if allowed. Returns 0 if allowed, otherwise the
     * number of seconds until the oldest in-window request expires.
     */
    private long tryAcquire(String clientId, long now) {
        Deque<Long> timestamps =
                requestLog.computeIfAbsent(clientId, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            long windowStart = now - windowMillis;
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequests) {
                long oldest = timestamps.peekFirst();
                long waitMillis = oldest + windowMillis - now;
                return Math.max(1, (waitMillis + 999) / 1000);
            }
            timestamps.addLast(now);
            return 0;
        }
    }

    private String clientId(HttpExchange exchange) {
        // If behind a trusted reverse proxy, prefer the X-Forwarded-For
        // header here instead. Do not trust it from direct internet traffic.
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void reject(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        byte[] body = "Too many requests. Please try again later.\n"
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Retry-After", Long.toString(retryAfterSeconds));
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private void evictStaleClients() {
        long windowStart = System.currentTimeMillis() - windowMillis;
        Iterator<Map.Entry<String, Deque<Long>>> it = requestLog.entrySet().iterator();
        while (it.hasNext()) {
            Deque<Long> timestamps = it.next().getValue();
            synchronized (timestamps) {
                while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
                    timestamps.pollFirst();
                }
                if (timestamps.isEmpty()) {
                    it.remove();
                }
            }
        }
    }
}
