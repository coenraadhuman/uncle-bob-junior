import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate) {
        this(delegate, 5, 60_000L);
    }

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowMillis) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowMillis;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        long now = System.currentTimeMillis();

        Window window = windows.computeIfAbsent(clientKey, k -> new Window());
        long retryAfterSeconds = window.tryAcquire(now, windowMillis, maxRequestsPerWindow);

        if (retryAfterSeconds >= 0) {
            sendTooManyRequests(exchange, retryAfterSeconds);
            return;
        }

        delegate.handle(exchange);
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private void sendTooManyRequests(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        byte[] body = "429 Too Many Requests\n".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    /** Call periodically (e.g. every few minutes) to evict stale per-client entries. */
    public void evictExpired() {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(e -> now - e.getValue().lastSeen() > windowMillis * 2);
    }

    /** Fixed-window request counter for a single client. */
    private final class Window {
        private long windowStart = 0L;
        private int count = 0;

        synchronized long lastSeen() {
            return windowStart;
        }

        /** Returns -1 if the request is allowed, otherwise seconds until retry. */
        synchronized long tryAcquire(long now, long windowMillis, int max) {
            if (windowStart == 0L || now - windowStart >= windowMillis) {
                windowStart = now;
                count = 0;
            }
            count++;
            if (count <= max) {
                return -1;
            }
            return Math.max(1, (windowStart + windowMillis - now) / 1000);
        }
    }
}
