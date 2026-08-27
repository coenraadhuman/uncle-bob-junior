import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wraps another HttpHandler and enforces a per-client fixed-window
 * request limit (default: 5 requests per 60-second window).
 */
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

        Window window = windows.computeIfAbsent(clientKey, k -> new Window(now));

        if (!window.tryAcquire(now, windowMillis, maxRequestsPerWindow)) {
            long retryAfterSeconds = Math.max(1, (window.windowStart.get() + windowMillis - now) / 1000);
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

    /** Periodically call this (e.g. from a scheduled task) to evict stale entries. */
    public void evictExpired() {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(e -> now - e.getValue().windowStart.get() > windowMillis * 2);
    }

    private static final class Window {
        final AtomicInteger count = new AtomicInteger(0);
        final AtomicInteger windowStart;

        Window(long now) {
            this.windowStart = new AtomicInteger((int) (now / 1000)); // seconds, avoids overflow concerns for our use
        }

        /** Not actually used; kept simple below with a synchronized fallback for correctness. */
        boolean tryAcquire(long now, long windowMillis, int max) {
            synchronized (this) {
                long start = startMillis;
                if (start == 0L || now - start >= windowMillis) {
                    startMillis = now;
                    count.set(0);
                }
                return count.incrementAndGet() <= max;
            }
        }

        volatile long startMillis;
    }
}
