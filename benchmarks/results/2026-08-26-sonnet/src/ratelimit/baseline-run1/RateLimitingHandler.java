import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wraps an HttpHandler and rejects requests once a client exceeds
 * maxRequests within windowMillis, using a per-client sliding window
 * of request timestamps.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;

    // Per-client request timestamp log (sliding window).
    private final Map<String, ClientWindow> clients = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limit-cleaner");
        t.setDaemon(true);
        return t;
    });

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;

        // Periodically drop clients that have been idle for longer than the
        // window, so memory doesn't grow unbounded with one-off visitors.
        cleaner.scheduleAtFixedRate(this::evictIdleClients, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    public static RateLimitingHandler perMinute(HttpHandler delegate, int maxRequestsPerMinute) {
        return new RateLimitingHandler(delegate, maxRequestsPerMinute, TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdOf(exchange);
        long now = System.currentTimeMillis();

        ClientWindow window = clients.computeIfAbsent(clientId, id -> new ClientWindow());

        if (window.tryAcquire(now, windowMillis, maxRequests)) {
            delegate.handle(exchange);
        } else {
            long retryAfterSeconds = (windowMillis / 1000) + 1;
            rejectWithTooManyRequests(exchange, retryAfterSeconds);
        }
    }

    public void shutdown() {
        cleaner.shutdownNow();
    }

    private void evictIdleClients() {
        long now = System.currentTimeMillis();
        clients.entrySet().removeIf(e -> e.getValue().isIdle(now, windowMillis));
    }

    private static String clientIdOf(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private static void rejectWithTooManyRequests(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        byte[] body = "Too Many Requests\n".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    /** Thread-safe sliding-window request log for a single client. */
    private static final class ClientWindow {
        private final Deque<Long> timestamps = new ConcurrentLinkedDeque<>();

        synchronized boolean tryAcquire(long now, long windowMillis, int maxRequests) {
            evictOlderThan(now - windowMillis);
            if (timestamps.size() >= maxRequests) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }

        synchronized boolean isIdle(long now, long windowMillis) {
            evictOlderThan(now - windowMillis);
            return timestamps.isEmpty();
        }

        private void evictOlderThan(long cutoff) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                timestamps.pollFirst();
            }
        }
    }

    // Example wiring with the JDK's built-in HttpServer.
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler applicationHandler = exchange -> {
            byte[] response = "OK\n".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        };

        // 5 requests per minute per client IP.
        RateLimitingHandler limited = RateLimitingHandler.perMinute(applicationHandler, 5);

        server.createContext("/", limited);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }
}
