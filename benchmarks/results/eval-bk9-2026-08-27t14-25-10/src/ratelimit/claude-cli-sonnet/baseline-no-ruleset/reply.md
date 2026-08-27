Assumptions: using the JDK's built-in `com.sun.net.httpserver.HttpHandler` (no external framework specified), rate-limiting keyed by client IP address, limit of 5 requests per 60-second window per client, returning HTTP 429 when exceeded, and a background sweep to evict idle client entries so memory doesn't grow unbounded.

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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wraps another HttpHandler and enforces a fixed-window rate limit
 * per client IP address.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Window> clients = new ConcurrentHashMap<>();
    private final ScheduledExecutorService sweeper = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limit-sweeper");
        t.setDaemon(true);
        return t;
    });

    public RateLimitingHandler(HttpHandler delegate) {
        this(delegate, 5, 60_000L);
    }

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowMillis) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowMillis;
        // Periodically drop windows that have expired and are no longer being touched,
        // otherwise the map grows for every distinct client that ever connected.
        this.sweeper.scheduleAtFixedRate(this::sweep, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        if (isAllowed(clientKey)) {
            delegate.handle(exchange);
        } else {
            sendTooManyRequests(exchange);
        }
    }

    private boolean isAllowed(String clientKey) {
        long now = System.currentTimeMillis();
        Window window = clients.computeIfAbsent(clientKey, k -> new Window(now));

        synchronized (window) {
            if (now - window.windowStart >= windowMillis) {
                window.windowStart = now;
                window.count.set(0);
            }
            return window.count.incrementAndGet() <= maxRequestsPerWindow;
        }
    }

    private void sweep() {
        long now = System.currentTimeMillis();
        clients.entrySet().removeIf(entry -> {
            Window w = entry.getValue();
            synchronized (w) {
                return now - w.windowStart >= windowMillis;
            }
        });
    }

    private static String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress() != null ? remote.getAddress().getHostAddress() : remote.toString();
    }

    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "429 Too Many Requests\n".getBytes();
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(windowMillis / 1000));
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    /** Shuts down the background sweeper thread; call when the server stops. */
    public void shutdown() {
        sweeper.shutdownNow();
    }

    private static final class Window {
        volatile long windowStart;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long windowStart) {
            this.windowStart = windowStart;
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

        HttpHandlerImpl actualHandler = new HttpHandlerImpl();
        RateLimitingHandler limited = new RateLimitingHandler(actualHandler, 5, 60_000L);

        server.createContext("/api", limited);
        server.setExecutor(null);
        server.start();
    }

    static final class HttpHandlerImpl implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            byte[] body = "OK\n".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        }
    }
}
```

Note: keying by remote IP is imperfect behind a shared NAT/proxy (many users share one IP) or if you need to trust `X-Forwarded-For` from a reverse proxy instead — swap `clientKey()` accordingly if that's your deployment topology.