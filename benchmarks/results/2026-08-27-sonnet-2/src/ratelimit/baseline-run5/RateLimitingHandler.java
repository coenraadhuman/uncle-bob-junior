import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final ConcurrentHashMap<String, ClientWindow> clients = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate) {
        this(delegate, 5, 60_000L);
    }

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowMillis) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowMillis;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        ClientWindow window = clients.computeIfAbsent(clientKey, k -> new ClientWindow());

        if (window.tryAcquire(maxRequestsPerWindow, windowMillis)) {
            delegate.handle(exchange);
        } else {
            sendTooManyRequests(exchange);
        }

        evictStaleEntriesOccasionally();
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress().getHostAddress();
    }

    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "Rate limit exceeded. Please slow down.".getBytes();
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(windowMillis / 1000));
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private void evictStaleEntriesOccasionally() {
        if (clients.size() < 10_000) {
            return;
        }
        long now = System.currentTimeMillis();
        clients.entrySet().removeIf(e -> now - e.getValue().windowStart.get() > windowMillis * 2);
    }

    private static final class ClientWindow {
        private final AtomicLong windowStart = new AtomicLong(0);
        private final AtomicInteger count = new AtomicInteger(0);

        synchronized boolean tryAcquire(int max, long windowMillis) {
            long now = System.currentTimeMillis();
            long start = windowStart.get();

            if (start == 0 || now - start >= windowMillis) {
                windowStart.set(now);
                count.set(1);
                return true;
            }

            if (count.get() < max) {
                count.incrementAndGet();
                return true;
            }

            return false;
        }
    }
}
