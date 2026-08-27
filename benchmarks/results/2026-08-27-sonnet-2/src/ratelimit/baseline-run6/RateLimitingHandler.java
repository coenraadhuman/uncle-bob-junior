import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wraps an HttpHandler with a simple fixed-window rate limiter,
 * keyed by client IP address.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    // Bound memory: drop entries not touched in a while, checked opportunistically.
    private final AtomicLong lastCleanup = new AtomicLong(System.currentTimeMillis());
    private final long cleanupIntervalMillis;

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerMinute) {
        this(delegate, maxRequestsPerMinute, 60_000L);
    }

    RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowMillis) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowMillis;
        this.cleanupIntervalMillis = windowMillis * 5;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        long now = System.currentTimeMillis();

        maybeCleanup(now);

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
        String body = "Rate limit exceeded. Try again later.";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(429, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void maybeCleanup(long now) {
        long last = lastCleanup.get();
        if (now - last < cleanupIntervalMillis) {
            return;
        }
        if (!lastCleanup.compareAndSet(last, now)) {
            return; // another thread is already cleaning up
        }
        windows.entrySet().removeIf(e -> now - e.getValue().windowStart.get() > cleanupIntervalMillis);
    }

    /** Per-client fixed window counter. */
    private static final class Window {
        final AtomicLong windowStart;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long start) {
            this.windowStart = new AtomicLong(start);
        }

        synchronized boolean tryAcquire(long now, long windowMillis, int max) {
            if (now - windowStart.get() >= windowMillis) {
                windowStart.set(now);
                count.set(0);
            }
            if (count.get() >= max) {
                return false;
            }
            count.incrementAndGet();
            return true;
        }
    }
}
