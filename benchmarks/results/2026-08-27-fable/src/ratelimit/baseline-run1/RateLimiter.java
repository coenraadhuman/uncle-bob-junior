import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding-window rate limiter keyed by client identifier (IP address).
 * Thread-safe: HttpServer may dispatch concurrent requests.
 */
final class RateLimiter {
    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    /**
     * Returns 0 if the request is allowed, otherwise the number of seconds
     * the client should wait before retrying.
     */
    long tryAcquire(String clientKey) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps =
                requestLog.computeIfAbsent(clientKey, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            long cutoff = now - windowMillis;
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= cutoff) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequests) {
                long oldest = timestamps.peekFirst();
                long retryAfterMillis = (oldest + windowMillis) - now;
                return Math.max(1, (retryAfterMillis + 999) / 1000);
            }
            timestamps.addLast(now);
            return 0;
        }
    }

    /** Drop entries for clients with no requests in the current window. */
    void evictStale() {
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

/** Wraps any HttpHandler and enforces the per-client rate limit. */
final class RateLimitingHandler implements HttpHandler {
    private final HttpHandler delegate;
    private final RateLimiter limiter;

    RateLimitingHandler(HttpHandler delegate, RateLimiter limiter) {
        this.delegate = delegate;
        this.limiter = limiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        long retryAfterSeconds = limiter.tryAcquire(clientKey);

        if (retryAfterSeconds > 0) {
            byte[] body = "Too many requests. Please try again later.\n"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Retry-After",
                    Long.toString(retryAfterSeconds));
            exchange.getResponseHeaders().set("Content-Type",
                    "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(429, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            return;
        }

        delegate.handle(exchange);
    }

    private static String clientKey(HttpExchange exchange) {
        // If behind a trusted reverse proxy, prefer X-Forwarded-For instead.
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}

public class Main {
    public static void main(String[] args) throws IOException {
        // 5 requests per client per 60 seconds
        RateLimiter limiter = new RateLimiter(5, 60_000);

        HttpHandler businessHandler = exchange -> {
            byte[] body = "Hello!\n".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        };

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new RateLimitingHandler(businessHandler, limiter));
        server.start();

        // Periodically evict idle clients so the map doesn't grow unbounded.
        Thread evictor = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                limiter.evictStale();
            }
        }, "rate-limiter-evictor");
        evictor.setDaemon(true);
        evictor.start();

        System.out.println("Server running on http://localhost:8080");
    }
}
