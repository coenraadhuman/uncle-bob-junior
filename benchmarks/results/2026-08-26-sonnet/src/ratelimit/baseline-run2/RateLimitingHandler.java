import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wraps an HttpHandler with a per-client sliding-window rate limit.
 * Not suitable for multi-instance deployments; state is process-local.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final long windowMillis;
    private final ConcurrentHashMap<String, ClientWindow> clients = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, long windowMillis) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowMillis;
    }

    public static RateLimitingHandler perMinute(HttpHandler delegate, int maxRequestsPerMinute) {
        return new RateLimitingHandler(delegate, maxRequestsPerMinute, 60_000L);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        ClientWindow window = clients.computeIfAbsent(clientKey, k -> new ClientWindow());

        long retryAfterSeconds = window.tryConsume(windowMillis, maxRequestsPerWindow);
        if (retryAfterSeconds >= 0) {
            sendTooManyRequests(exchange, retryAfterSeconds);
            return;
        }

        maybeCleanup();
        delegate.handle(exchange);
    }

    private static String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress() != null ? remote.getAddress().getHostAddress() : remote.toString();
    }

    private static void sendTooManyRequests(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        byte[] body = "Rate limit exceeded. Please slow down.".getBytes();
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    // Occasionally sweep clients with no recent activity so the map doesn't grow unbounded.
    private volatile long lastCleanup = 0L;
    private void maybeCleanup() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < windowMillis) {
            return;
        }
        lastCleanup = now;
        for (Iterator<ConcurrentHashMap.Entry<String, ClientWindow>> it = clients.entrySet().iterator(); it.hasNext(); ) {
            ConcurrentHashMap.Entry<String, ClientWindow> entry = it.next();
            if (entry.getValue().isStale(now, windowMillis)) {
                it.remove();
            }
        }
    }

    /** Tracks request timestamps for one client within the sliding window. */
    private static final class ClientWindow {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private final ReentrantLock lock = new ReentrantLock();

        /**
         * @return -1 if the request is allowed, otherwise seconds until the client may retry.
         */
        long tryConsume(long windowMillis, int maxRequests) {
            lock.lock();
            try {
                long now = System.currentTimeMillis();
                evictOlderThan(now - windowMillis);

                if (timestamps.size() >= maxRequests) {
                    long oldest = timestamps.peekFirst();
                    long retryAfterMillis = windowMillis - (now - oldest);
                    return Math.max(1, (retryAfterMillis + 999) / 1000);
                }

                timestamps.addLast(now);
                return -1;
            } finally {
                lock.unlock();
            }
        }

        boolean isStale(long now, long windowMillis) {
            lock.lock();
            try {
                evictOlderThan(now - windowMillis);
                return timestamps.isEmpty();
            } finally {
                lock.unlock();
            }
        }

        private void evictOlderThan(long cutoff) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                timestamps.pollFirst();
            }
        }
    }
}
