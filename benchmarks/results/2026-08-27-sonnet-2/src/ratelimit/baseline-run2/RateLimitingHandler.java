import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wraps an HttpHandler with a per-client rate limit using a fixed-window counter.
 */
public class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Window> clients = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limit-cleaner");
        t.setDaemon(true);
        return t;
    });

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowMillis) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowMillis;
        // Periodically drop windows that have expired so the map doesn't grow unbounded.
        cleaner.scheduleAtFixedRate(this::evictExpired, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    public static RateLimitingHandler perMinute(HttpHandler delegate, int maxRequestsPerMinute) {
        return new RateLimitingHandler(delegate, maxRequestsPerMinute, TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        if (!allow(clientKey)) {
            byte[] body = "Rate limit exceeded. Please try again later.".getBytes();
            exchange.getResponseHeaders().set("Retry-After", String.valueOf(windowMillis / 1000));
            exchange.sendResponseHeaders(429, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
            return;
        }
        delegate.handle(exchange);
    }

    private boolean allow(String clientKey) {
        long now = System.currentTimeMillis();
        Window window = clients.computeIfAbsent(clientKey, k -> new Window(now));

        synchronized (window) {
            if (now - window.startTime >= windowMillis) {
                window.startTime = now;
                window.count.set(0);
            }
            return window.count.incrementAndGet() <= maxRequestsPerWindow;
        }
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        clients.entrySet().removeIf(entry -> {
            Window w = entry.getValue();
            synchronized (w) {
                return now - w.startTime >= windowMillis;
            }
        });
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    public void shutdown() {
        cleaner.shutdown();
    }

    private static final class Window {
        volatile long startTime;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long startTime) {
            this.startTime = startTime;
        }
    }

    // Example wiring:
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler helloHandler = exchange -> {
            byte[] body = "Hello, world!".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        };

        server.createContext("/hello", RateLimitingHandler.perMinute(helloHandler, 5));
        server.setExecutor(null);
        server.start();
    }
}
