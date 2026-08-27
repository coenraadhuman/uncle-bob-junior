import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Wraps a delegate HttpHandler with a per-client sliding-window rate limit.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;

        // Periodically evict clients that haven't made a request recently,
        // so the map doesn't grow forever.
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleanup");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(this::evictStaleEntries, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    public RateLimitingHandler(HttpHandler delegate) {
        this(delegate, 5, TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientKey(exchange);
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = requestLog.computeIfAbsent(clientId, k -> new ArrayDeque<>());

        boolean allowed;
        long retryAfterSeconds = 0;

        synchronized (timestamps) {
            evictOlderThan(timestamps, now - windowMillis);

            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                allowed = true;
            } else {
                allowed = false;
                long oldest = timestamps.peekFirst();
                retryAfterSeconds = Math.max(1, (windowMillis - (now - oldest)) / 1000 + 1);
            }
        }

        if (allowed) {
            delegate.handle(exchange);
        } else {
            sendTooManyRequests(exchange, retryAfterSeconds);
        }
    }

    private void evictOlderThan(Deque<Long> timestamps, long cutoff) {
        while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
            timestamps.pollFirst();
        }
    }

    private void evictStaleEntries() {
        long cutoff = System.currentTimeMillis() - windowMillis;
        for (Iterator<Map.Entry<String, Deque<Long>>> it = requestLog.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Deque<Long>> entry = it.next();
            Deque<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                evictOlderThan(timestamps, cutoff);
                if (timestamps.isEmpty()) {
                    it.remove();
                }
            }
        }
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private void sendTooManyRequests(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        String body = "429 Too Many Requests - rate limit exceeded, retry later\n";
        byte[] bytes = body.getBytes();

        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Example wiring
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler helloHandler = exchange -> {
            byte[] response = "Hello, world!\n".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        };

        server.createContext("/hello", new RateLimitingHandler(helloHandler, 5, TimeUnit.MINUTES.toMillis(1)));
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
    }
}
