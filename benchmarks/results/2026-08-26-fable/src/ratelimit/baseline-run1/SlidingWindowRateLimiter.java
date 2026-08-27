import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Sliding-window rate limiter keyed by client identifier.
 * Thread-safe; suitable for a multi-threaded HttpServer.
 */
final class SlidingWindowRateLimiter {

    private final int maxRequests;
    private final long windowNanos;
    private final ConcurrentMap<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    SlidingWindowRateLimiter(int maxRequests, long window, TimeUnit unit) {
        this.maxRequests = maxRequests;
        this.windowNanos = unit.toNanos(window);
    }

    /**
     * Records an attempt for the given client and reports whether it is allowed.
     *
     * @return 0 if allowed, otherwise the number of seconds until the client
     *         may retry (suitable for a Retry-After header).
     */
    long tryAcquire(String clientId) {
        long now = System.nanoTime();
        Deque<Long> timestamps = requestLog.computeIfAbsent(clientId, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            // Evict entries that have fallen out of the window.
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= windowNanos) {
                timestamps.pollFirst();
            }
            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                return 0;
            }
            long oldest = timestamps.peekFirst();
            long retryAfterNanos = windowNanos - (now - oldest);
            return Math.max(1, TimeUnit.NANOSECONDS.toSeconds(retryAfterNanos) + 1);
        }
    }

    /** Removes clients with no requests inside the current window. Call periodically. */
    void evictIdleClients() {
        long now = System.nanoTime();
        requestLog.forEach((clientId, timestamps) -> {
            synchronized (timestamps) {
                while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= windowNanos) {
                    timestamps.pollFirst();
                }
                if (timestamps.isEmpty()) {
                    // Remove only if still the same (empty) deque, to avoid
                    // discarding a concurrent insertion.
                    requestLog.remove(clientId, timestamps);
                }
            }
        });
    }
}

/**
 * Decorator that applies rate limiting before delegating to the real handler.
 */
final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final SlidingWindowRateLimiter limiter;
    private final boolean trustForwardedHeader;

    RateLimitingHandler(HttpHandler delegate, SlidingWindowRateLimiter limiter, boolean trustForwardedHeader) {
        this.delegate = delegate;
        this.limiter = limiter;
        this.trustForwardedHeader = trustForwardedHeader;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = resolveClientId(exchange);
        long retryAfterSeconds = limiter.tryAcquire(clientId);

        if (retryAfterSeconds > 0) {
            byte[] body = "Too many requests. Please try again later.\n"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Retry-After", Long.toString(retryAfterSeconds));
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(429, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            return;
        }

        delegate.handle(exchange);
    }

    private String resolveClientId(HttpExchange exchange) {
        if (trustForwardedHeader) {
            String forwarded = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                // First entry is the originating client.
                return forwarded.split(",")[0].trim();
            }
        }
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}

public class RateLimitedServer {

    public static void main(String[] args) throws IOException {
        SlidingWindowRateLimiter limiter =
                new SlidingWindowRateLimiter(5, 1, TimeUnit.MINUTES);

        HttpHandler businessHandler = exchange -> {
            byte[] body = "Hello from the handler!\n".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        };

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // Set trustForwardedHeader to true only when running behind a trusted proxy.
        server.createContext("/api", new RateLimitingHandler(businessHandler, limiter, false));
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        // Housekeeping: drop idle clients so the map does not grow unbounded.
        var cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limiter-cleanup");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(limiter::evictIdleClients, 1, 1, TimeUnit.MINUTES);

        System.out.println("Server listening on http://localhost:8080/api");
    }
}
