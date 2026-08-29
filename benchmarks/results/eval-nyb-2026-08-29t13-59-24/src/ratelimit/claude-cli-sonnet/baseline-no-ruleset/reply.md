A few assumptions I'm making: this targets `com.sun.net.httpserver.HttpHandler` (JDK's built-in HTTP server, no external deps), clients are identified by remote IP address, the limit is a token bucket allowing 5 requests per 60-second window per client, and it returns HTTP 429 with a `Retry-After` header when exceeded. Idle client entries are periodically evicted to avoid unbounded memory growth.

```java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wraps an HttpHandler with a per-client token-bucket rate limiter.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limiter-cleanup");
        t.setDaemon(true);
        return t;
    });

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowDuration, TimeUnit windowUnit) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowUnit.toMillis(windowDuration);
        // Evict buckets that have been idle for 2 windows, run every window.
        cleaner.scheduleAtFixedRate(this::evictIdleBuckets, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    public static RateLimitingHandler perMinute(HttpHandler delegate, int maxRequestsPerMinute) {
        return new RateLimitingHandler(delegate, maxRequestsPerMinute, 1, TimeUnit.MINUTES);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        TokenBucket bucket = buckets.computeIfAbsent(clientKey, k -> new TokenBucket(maxRequestsPerWindow));

        if (bucket.tryConsume(maxRequestsPerWindow, windowMillis)) {
            delegate.handle(exchange);
        } else {
            sendTooManyRequests(exchange);
        }
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress() != null ? remote.getAddress().getHostAddress() : remote.toString();
    }

    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        String body = "429 Too Many Requests\n";
        byte[] bytes = body.getBytes();
        exchange.getResponseHeaders().set("Retry-After", String.valueOf(windowMillis / 1000));
        exchange.sendResponseHeaders(429, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void evictIdleBuckets() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(e -> now - e.getValue().lastRefill.get() > windowMillis * 2);
    }

    /** Simple lazily-refilling token bucket, thread-safe via synchronization. */
    private static final class TokenBucket {
        private double tokens;
        private final AtomicLong lastRefill = new AtomicLong(System.currentTimeMillis());

        TokenBucket(int initialTokens) {
            this.tokens = initialTokens;
        }

        synchronized boolean tryConsume(int capacity, long windowMillis) {
            long now = System.currentTimeMillis();
            long last = lastRefill.get();
            long elapsed = now - last;

            if (elapsed > 0) {
                double refillRate = capacity / (double) windowMillis; // tokens per ms
                tokens = Math.min(capacity, tokens + elapsed * refillRate);
                lastRefill.set(now);
            }

            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
    }
}
```

Usage example:

```java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        HttpHandler apiHandler = exchange -> {
            byte[] response = "Hello".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        };

        server.createContext("/api", RateLimitingHandler.perMinute(apiHandler, 5));
        server.start();
    }
}
```

Notes:
- Rate limiting by remote IP is easy to spoof behind proxies/NAT; if requests pass through a load balancer, key off an authenticated client/API-key identifier instead, or trust `X-Forwarded-For` only from a known proxy.
- This is in-memory and per-instance; for a multi-instance deployment you'd need a shared store (e.g. Redis) to enforce limits globally.