Here's a complete solution. Assumptions: you're using the JDK's built-in `com.sun.net.httpserver` (no external frameworks), clients are identified by IP address, and the limit is 5 requests per minute using a sliding window. All of these are easy to adjust.

```java
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding-window rate limiter, keyed by client identifier.
 * Thread-safe and self-cleaning: idle clients are evicted lazily.
 */
public final class RateLimiter {

    private final int maxRequests;
    private final long windowNanos;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.windowNanos = window.toNanos();
    }

    /** Returns true if the request is allowed, false if the client is over the limit. */
    public boolean tryAcquire(String clientId) {
        long now = System.nanoTime();
        Window w = windows.computeIfAbsent(clientId, k -> new Window(maxRequests));
        boolean allowed = w.tryAcquire(now, windowNanos);
        // Occasionally sweep out entries for clients that have gone quiet,
        // so the map does not grow without bound.
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(e -> e.getValue().isIdle(now, windowNanos));
        }
        return allowed;
    }

    /** Seconds until the oldest tracked request leaves the window; used for Retry-After. */
    public long retryAfterSeconds(String clientId) {
        Window w = windows.get(clientId);
        if (w == null) {
            return 0;
        }
        return w.retryAfterSeconds(System.nanoTime(), windowNanos);
    }

    /** Ring buffer of the timestamps of the last maxRequests requests. */
    private static final class Window {
        private final long[] timestamps;
        private int index;
        private int count;

        Window(int capacity) {
            this.timestamps = new long[capacity];
        }

        synchronized boolean tryAcquire(long now, long windowNanos) {
            if (count == timestamps.length) {
                long oldest = timestamps[index];
                if (now - oldest < windowNanos) {
                    return false;
                }
                // Oldest request has aged out; overwrite it.
                timestamps[index] = now;
                index = (index + 1) % timestamps.length;
                return true;
            }
            timestamps[(index + count) % timestamps.length] = now;
            count++;
            return true;
        }

        synchronized long retryAfterSeconds(long now, long windowNanos) {
            if (count < timestamps.length) {
                return 0;
            }
            long oldest = timestamps[index];
            long remaining = windowNanos - (now - oldest);
            return remaining <= 0 ? 0 : Math.max(1, remaining / 1_000_000_000L);
        }

        synchronized boolean isIdle(long now, long windowNanos) {
            if (count == 0) {
                return true;
            }
            int newest = (index + count - 1) % timestamps.length;
            return now - timestamps[newest] > windowNanos;
        }
    }
}
```

```java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Wraps any HttpHandler with per-client rate limiting.
 * Over-limit requests get 429 Too Many Requests with a Retry-After header.
 */
public final class RateLimitedHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final RateLimiter limiter;

    public RateLimitedHandler(HttpHandler delegate, RateLimiter limiter) {
        this.delegate = delegate;
        this.limiter = limiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientId(exchange);

        if (limiter.tryAcquire(clientId)) {
            delegate.handle(exchange);
            return;
        }

        byte[] body = "Too many requests. Please try again later.\n"
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Retry-After",
                String.valueOf(limiter.retryAfterSeconds(clientId)));
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private static String clientId(HttpExchange exchange) {
        // If you sit behind a trusted reverse proxy, prefer the first entry of
        // X-Forwarded-For here instead. Do not trust that header from the open
        // internet, since clients can spoof it to dodge the limit.
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}
```

```java
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;

/** Example wiring: 5 requests per client per minute. */
public final class Main {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        RateLimiter limiter = new RateLimiter(5, Duration.ofMinutes(1));

        server.createContext("/api", new RateLimitedHandler(exchange -> {
            byte[] body = "Hello!\n".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }, limiter));

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("Listening on http://localhost:8080/api");
    }
}
```

How it works:

- **Sliding window per client.** Each client gets a small ring buffer holding the timestamps of its last N requests. A request is allowed only if the oldest of those timestamps has aged out of the window. This avoids the burst-at-the-boundary problem of a fixed per-minute counter (10 requests in 2 seconds straddling a minute boundary).
- **Thread safety.** The map is a `ConcurrentHashMap` and each per-client window synchronises on itself, so contention is per client rather than global.
- **Memory.** Idle client entries are swept lazily once the map grows past 10,000 entries, so a long-running server does not leak memory from one-off visitors.
- **Proper HTTP semantics.** Rejected requests get status 429 with a `Retry-After` header telling the client when to retry.

If you're using Spring, Jakarta Servlets, or another framework rather than `com.sun.net.httpserver`, the `RateLimiter` class carries over unchanged; only the wrapper (a servlet `Filter` or Spring `HandlerInterceptor`) would differ. Note also that per-IP limiting shares one bucket among users behind the same NAT or corporate proxy; if your API is authenticated, keying on the user or API token is fairer.