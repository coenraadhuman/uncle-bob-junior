Assumptions: using the built-in `com.sun.net.httpserver.HttpHandler` (JDK, no external framework specified), keying clients by remote IP address, and allowing 5 requests per 60-second sliding window per client. Returns HTTP 429 when the limit is exceeded, and periodically evicts idle client entries to avoid unbounded memory growth.

```java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wraps an HttpHandler and rejects requests once a client exceeds
 * maxRequests within windowMillis, using a sliding-window log per client.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, ClientState> clients = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;

        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(this::evictIdleClients, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientKey(exchange);
        ClientState state = clients.computeIfAbsent(clientId, k -> new ClientState());

        if (state.tryAcquire(maxRequests, windowMillis)) {
            delegate.handle(exchange);
        } else {
            sendTooManyRequests(exchange);
        }
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "429 Too Many Requests".getBytes();
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(windowMillis / 1000));
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private void evictIdleClients() {
        long cutoff = System.currentTimeMillis() - windowMillis;
        clients.entrySet().removeIf(e -> e.getValue().lastRequestBefore(cutoff));
    }

    /** Per-client sliding window of request timestamps. */
    private static final class ClientState {
        private final Deque<Long> timestamps = new ArrayDeque<>();

        synchronized boolean tryAcquire(int maxRequests, long windowMillis) {
            long now = System.currentTimeMillis();
            long windowStart = now - windowMillis;

            while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= maxRequests) {
                return false;
            }

            timestamps.addLast(now);
            return true;
        }

        synchronized boolean lastRequestBefore(long cutoff) {
            return timestamps.isEmpty() || timestamps.peekLast() < cutoff;
        }
    }
}
```

Example wiring:

```java
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public final class Server {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler helloHandler = exchange -> {
            byte[] body = "Hello".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        };

        // 5 requests per 60 seconds per client IP
        server.createContext("/", new RateLimitingHandler(helloHandler, 5, 60_000));
        server.start();
    }
}
```

Notes:
- Client identity uses the remote socket IP. If you're behind a reverse proxy, use the `X-Forwarded-For` header instead (only trust it if the proxy is trusted, to avoid spoofing).
- `synchronized` on `ClientState` is fine at this scale (a few requests/minute per client); for very high traffic, consider `AtomicLong`/lock-free counters or an off-the-shelf library (e.g. Bucket4j, Guava `RateLimiter`) instead of the hand-rolled sliding log.