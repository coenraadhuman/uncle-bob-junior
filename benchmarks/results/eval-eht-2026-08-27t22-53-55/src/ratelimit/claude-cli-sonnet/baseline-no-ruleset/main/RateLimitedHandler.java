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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wraps an HttpHandler with a per-client sliding-window rate limit.
 */
public final class RateLimitedHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limit-cleanup");
        t.setDaemon(true);
        return t;
    });

    public RateLimitedHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        cleaner.scheduleAtFixedRate(this::purgeStaleEntries, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdFor(exchange);
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = requestLog.computeIfAbsent(clientId, k -> new ArrayDeque<>());

        boolean allowed;
        long retryAfterMillis = 0;

        synchronized (timestamps) {
            evictOld(timestamps, now);
            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                allowed = true;
            } else {
                allowed = false;
                retryAfterMillis = windowMillis - (now - timestamps.peekFirst());
            }
        }

        if (!allowed) {
            rejectTooManyRequests(exchange, retryAfterMillis);
            return;
        }

        delegate.handle(exchange);
    }

    private void evictOld(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= windowMillis) {
            timestamps.pollFirst();
        }
    }

    private void purgeStaleEntries() {
        long now = System.currentTimeMillis();
        for (Iterator<Map.Entry<String, Deque<Long>>> it = requestLog.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Deque<Long>> entry = it.next();
            Deque<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                evictOld(timestamps, now);
                if (timestamps.isEmpty()) {
                    it.remove();
                }
            }
        }
    }

    private String clientIdFor(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress() != null ? remote.getAddress().getHostAddress() : remote.toString();
    }

    private void rejectTooManyRequests(HttpExchange exchange, long retryAfterMillis) throws IOException {
        long retryAfterSeconds = Math.max(1, retryAfterMillis / 1000);
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        byte[] body = "429 Too Many Requests".getBytes();
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    public void shutdown() {
        cleaner.shutdown();
    }

    // Example wiring
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler helloHandler = exchange -> {
            byte[] body = "Hello".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        };

        server.createContext("/hello", new RateLimitedHandler(helloHandler, 5, 60_000));
        server.start();
    }
}
