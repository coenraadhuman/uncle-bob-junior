import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.concurrent.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wraps an HttpHandler with a per-client sliding-window rate limit.
 * Not suitable for multi-instance deployments without a shared store.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limit-cleaner");
        t.setDaemon(true);
        return t;
    });

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowMillis) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowMillis;
        // Periodically drop clients with no recent activity so the map doesn't grow forever.
        cleaner.scheduleAtFixedRate(this::evictStaleClients, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    public static RateLimitingHandler perMinute(HttpHandler delegate, int maxRequestsPerMinute) {
        return new RateLimitingHandler(delegate, maxRequestsPerMinute, TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        long now = System.currentTimeMillis();

        if (!tryConsume(clientKey, now)) {
            sendTooManyRequests(exchange);
            return;
        }

        delegate.handle(exchange);
    }

    private boolean tryConsume(String clientKey, long now) {
        Deque<Long> timestamps = requestLog.computeIfAbsent(clientKey, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            long windowStart = now - windowMillis;
            while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequestsPerWindow) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    private void evictStaleClients(long nowUnused) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowMillis;
        requestLog.entrySet().removeIf(entry -> {
            Deque<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                    timestamps.pollFirst();
                }
                return timestamps.isEmpty();
            }
        });
    }

    private void evictStaleClients() {
        evictStaleClients(0L);
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "429 Too Many Requests\n".getBytes();
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(windowMillis / 1000));
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    public void shutdown() {
        cleaner.shutdown();
    }
}
