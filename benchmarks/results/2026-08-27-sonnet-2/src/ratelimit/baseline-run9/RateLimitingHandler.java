import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wraps an HttpHandler with a per-client sliding-window rate limit.
 * Each client (identified by remote IP) may make at most MAX_REQUESTS
 * requests within WINDOW_MILLIS.
 */
public final class RateLimitingHandler implements HttpHandler {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = TimeUnit.MINUTES.toMillis(1);
    private static final long IDLE_EVICTION_MILLIS = TimeUnit.MINUTES.toMillis(10);

    private final HttpHandler delegate;
    private final Map<String, ClientRecord> clients = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limiter-cleanup");
        t.setDaemon(true);
        return t;
    });

    public RateLimitingHandler(HttpHandler delegate) {
        this.delegate = delegate;
        cleaner.scheduleAtFixedRate(this::evictIdleClients, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = resolveClientId(exchange);
        ClientRecord record = clients.computeIfAbsent(clientId, k -> new ClientRecord());

        long retryAfterSeconds;
        synchronized (record) {
            long now = System.currentTimeMillis();
            record.lastSeen = now;
            evictOldTimestamps(record.timestamps, now);

            if (record.timestamps.size() >= MAX_REQUESTS) {
                long oldest = record.timestamps.peekFirst();
                retryAfterSeconds = Math.max(1, (WINDOW_MILLIS - (now - oldest)) / 1000 + 1);
            } else {
                record.timestamps.addLast(now);
                retryAfterSeconds = -1;
            }
        }

        if (retryAfterSeconds >= 0) {
            rejectWithTooManyRequests(exchange, retryAfterSeconds);
            return;
        }

        delegate.handle(exchange);
    }

    private static void evictOldTimestamps(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= WINDOW_MILLIS) {
            timestamps.pollFirst();
        }
    }

    private void evictIdleClients() {
        long now = System.currentTimeMillis();
        clients.entrySet().removeIf(e -> now - e.getValue().lastSeen > IDLE_EVICTION_MILLIS);
    }

    private void rejectWithTooManyRequests(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        byte[] body = "Rate limit exceeded. Please slow down.".getBytes("UTF-8");
        exchange.getResponseHeaders().set("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, body.length);
        try (var os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private static String resolveClientId(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress() != null ? remote.getAddress().getHostAddress() : remote.toString();
    }

    public void shutdown() {
        cleaner.shutdownNow();
    }

    private static final class ClientRecord {
        final Deque<Long> timestamps = new ArrayDeque<>();
        volatile long lastSeen = System.currentTimeMillis();
    }
}
