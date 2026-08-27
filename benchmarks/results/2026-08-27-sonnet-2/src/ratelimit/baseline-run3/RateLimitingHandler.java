import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Wraps an HttpHandler with a per-client sliding-window rate limit.
 * Not suitable for multi-instance deployments without a shared store (e.g. Redis),
 * since state is kept in memory per process.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, ClientState> clients = new ConcurrentHashMap<>();

    // Bound how many idle client entries we keep around before sweeping.
    private final int maxTrackedClients;

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this(delegate, maxRequests, windowMillis, 10_000);
    }

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis, int maxTrackedClients) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.maxTrackedClients = maxTrackedClients;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = resolveClientId(exchange);
        ClientState state = clients.computeIfAbsent(clientId, id -> new ClientState());

        if (!state.tryAcquire(maxRequests, windowMillis)) {
            sendTooManyRequests(exchange);
            return;
        }

        maybeEvictStaleClients();
        delegate.handle(exchange);
    }

    private String resolveClientId(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote != null && remote.getAddress() != null
                ? remote.getAddress().getHostAddress()
                : "unknown";
    }

    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "Too Many Requests\n".getBytes();
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(windowMillis / 1000));
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    // Cheap opportunistic cleanup; avoids a dedicated background thread.
    private void maybeEvictStaleClients() {
        if (clients.size() <= maxTrackedClients) {
            return;
        }
        long cutoff = System.currentTimeMillis() - windowMillis;
        clients.entrySet().removeIf(e -> e.getValue().isStaleSince(cutoff));
    }

    private static final class ClientState {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private final ReentrantLock lock = new ReentrantLock();

        boolean tryAcquire(int maxRequests, long windowMillis) {
            long now = System.currentTimeMillis();
            lock.lock();
            try {
                long cutoff = now - windowMillis;
                while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                    timestamps.pollFirst();
                }
                if (timestamps.size() >= maxRequests) {
                    return false;
                }
                timestamps.addLast(now);
                return true;
            } finally {
                lock.unlock();
            }
        }

        boolean isStaleSince(long cutoff) {
            lock.lock();
            try {
                return timestamps.isEmpty() || timestamps.peekLast() < cutoff;
            } finally {
                lock.unlock();
            }
        }
    }

    // Example wiring:
    //
    // HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    // server.createContext("/api", new RateLimitingHandler(realHandler, 5, 60_000));
    // server.start();
}
