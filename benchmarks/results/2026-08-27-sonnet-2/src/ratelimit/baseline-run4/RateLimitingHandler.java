import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wraps an HttpHandler with a per-client sliding-window rate limit.
 * Client identity is derived from the remote IP address.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Map<String, ClientState> clients = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate, int maxRequestsPerWindow, Duration window) {
        this.delegate = delegate;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
    }

    public static RateLimitingHandler perMinute(HttpHandler delegate, int maxRequestsPerMinute) {
        return new RateLimitingHandler(delegate, maxRequestsPerMinute, Duration.ofMinutes(1));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdFor(exchange);
        ClientState state = clients.computeIfAbsent(clientId, id -> new ClientState());

        long retryAfterMillis = state.recordAndCheck(window.toMillis(), maxRequestsPerWindow);
        if (retryAfterMillis >= 0) {
            sendTooManyRequests(exchange, retryAfterMillis);
            return;
        }

        delegate.handle(exchange);
    }

    private static String clientIdFor(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private static void sendTooManyRequests(HttpExchange exchange, long retryAfterMillis) throws IOException {
        long retryAfterSeconds = Math.max(1, (retryAfterMillis + 999) / 1000);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Retry-After", String.valueOf(retryAfterSeconds));
        headers.set("Content-Type", "text/plain; charset=utf-8");

        byte[] body = "429 Too Many Requests".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    /**
     * Tracks request timestamps for one client using a sliding window.
     * Guarded by a lock rather than a concurrent collection because the
     * check-and-evict-and-record sequence must be atomic.
     */
    private static final class ClientState {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private final ReentrantLock lock = new ReentrantLock();

        /**
         * Returns -1 if the request is allowed (and records it), otherwise
         * returns the number of milliseconds until the next slot frees up.
         */
        long recordAndCheck(long windowMillis, int maxRequests) {
            lock.lock();
            try {
                long now = System.currentTimeMillis();
                long windowStart = now - windowMillis;

                while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
                    timestamps.pollFirst();
                }

                if (timestamps.size() >= maxRequests) {
                    long oldest = timestamps.peekFirst();
                    return (oldest + windowMillis) - now;
                }

                timestamps.addLast(now);
                return -1;
            } finally {
                lock.unlock();
            }
        }
    }
}
