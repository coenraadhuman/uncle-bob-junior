import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.concurrent.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wraps another handler and rejects requests once a client exceeds
 * maxRequests within windowMillis, sliding-window style.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, ClientWindow> clients = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;

        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(this::evictStale, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    public static RateLimitingHandler perMinute(HttpHandler delegate, int maxRequestsPerMinute) {
        return new RateLimitingHandler(delegate, maxRequestsPerMinute, TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientId(exchange);
        ClientWindow window = clients.computeIfAbsent(clientId, k -> new ClientWindow());

        if (window.tryAcquire(maxRequests, windowMillis)) {
            delegate.handle(exchange);
        } else {
            sendTooManyRequests(exchange, windowMillis);
        }
    }

    private String clientId(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress() != null ? remote.getAddress().getHostAddress() : remote.getHostString();
    }

    private void sendTooManyRequests(HttpExchange exchange, long windowMillis) throws IOException {
        String body = "Too Many Requests";
        byte[] bytes = body.getBytes();
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(windowMillis / 1000));
        exchange.sendResponseHeaders(429, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void evictStale() {
        long cutoff = System.currentTimeMillis() - windowMillis;
        clients.entrySet().removeIf(e -> e.getValue().isStale(cutoff));
    }

    private static final class ClientWindow {
        private final Deque<Long> timestamps = new ArrayDeque<>();

        synchronized boolean tryAcquire(int maxRequests, long windowMillis) {
            long now = System.currentTimeMillis();
            long cutoff = now - windowMillis;
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= cutoff) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequests) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }

        synchronized boolean isStale(long cutoff) {
            return timestamps.isEmpty() || timestamps.peekLast() <= cutoff;
        }
    }
}
