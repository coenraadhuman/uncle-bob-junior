import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wraps an HttpHandler and rejects requests once a client exceeds
 * maxRequests within a rolling windowMillis period.
 */
public final class RateLimitedHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, ClientState> clients = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limiter-cleanup");
        t.setDaemon(true);
        return t;
    });

    public RateLimitedHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        // periodically evict clients idle longer than the window, so memory
        // doesn't grow unbounded with one-off visitors
        cleaner.scheduleAtFixedRate(this::evictStale, windowMillis, windowMillis, TimeUnit.MILLISECONDS);
    }

    public static RateLimitedHandler perMinute(HttpHandler delegate, int maxRequestsPerMinute) {
        return new RateLimitedHandler(delegate, maxRequestsPerMinute, TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientKey(exchange);
        ClientState state = clients.computeIfAbsent(clientId, k -> new ClientState());

        if (!state.tryAcquire(maxRequests, windowMillis)) {
            long retryAfterSeconds = (windowMillis + 999) / 1000;
            exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
            byte[] body = "429 Too Many Requests\n".getBytes();
            exchange.sendResponseHeaders(429, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            return;
        }

        delegate.handle(exchange);
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress() != null ? remote.getAddress().getHostAddress() : remote.toString();
    }

    private void evictStale() {
        long now = System.currentTimeMillis();
        clients.entrySet().removeIf(e -> e.getValue().isStale(now, windowMillis));
    }

    public void shutdown() {
        cleaner.shutdown();
    }

    /** Fixed-window counter per client, reset once the window elapses. */
    private static final class ClientState {
        private final ReentrantLock lock = new ReentrantLock();
        private long windowStart = System.currentTimeMillis();
        private int count = 0;

        boolean tryAcquire(int maxRequests, long windowMillis) {
            lock.lock();
            try {
                long now = System.currentTimeMillis();
                if (now - windowStart >= windowMillis) {
                    windowStart = now;
                    count = 0;
                }
                if (count >= maxRequests) {
                    return false;
                }
                count++;
                return true;
            } finally {
                lock.unlock();
            }
        }

        boolean isStale(long now, long windowMillis) {
            lock.lock();
            try {
                return now - windowStart >= windowMillis;
            } finally {
                lock.unlock();
            }
        }
    }
}
