import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.concurrent.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wraps an HttpHandler with a per-client sliding-window rate limit.
 * Clients are identified by remote IP address.
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
    }

    /** Convenience constructor: 5 requests per 60 seconds. */
    public RateLimitingHandler(HttpHandler delegate) {
        this(delegate, 5, 60_000L);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        ClientWindow window = clients.computeIfAbsent(clientKey, k -> new ClientWindow());

        if (window.tryAcquire(maxRequests, windowMillis)) {
            try {
                delegate.handle(exchange);
            } finally {
                maybeCleanup();
            }
        } else {
            sendTooManyRequests(exchange, windowMillis);
        }
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private void sendTooManyRequests(HttpExchange exchange, long windowMillis) throws IOException {
        byte[] body = "Too Many Requests\n".getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.getResponseHeaders().set("Retry-After", Long.toString(windowMillis / 1000));
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    // Periodically drop client entries with no recent activity to bound memory use.
    private volatile long lastCleanup = 0L;

    private void maybeCleanup() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < windowMillis) {
            return;
        }
        lastCleanup = now;
        clients.entrySet().removeIf(e -> e.getValue().isStale(now, windowMillis));
    }

    /** Tracks request timestamps for one client within the sliding window. */
    private static final class ClientWindow {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private final ReentrantLock lock = new ReentrantLock();

        boolean tryAcquire(int maxRequests, long windowMillis) {
            long now = System.currentTimeMillis();
            lock.lock();
            try {
                evictOld(now, windowMillis);
                if (timestamps.size() >= maxRequests) {
                    return false;
                }
                timestamps.addLast(now);
                return true;
            } finally {
                lock.unlock();
            }
        }

        boolean isStale(long now, long windowMillis) {
            lock.lock();
            try {
                evictOld(now, windowMillis);
                return timestamps.isEmpty();
            } finally {
                lock.unlock();
            }
        }

        private void evictOld(long now, long windowMillis) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMillis) {
                timestamps.pollFirst();
            }
        }
    }
}
