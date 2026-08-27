import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Wraps an HttpHandler with a per-client token-bucket rate limit.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final int capacity;          // max burst size (tokens)
    private final double refillPerNano;  // tokens added per nanosecond
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limiter-cleanup");
        t.setDaemon(true);
        return t;
    });

    /**
     * @param delegate        handler to protect
     * @param maxRequests     max requests allowed per client per minute
     */
    public RateLimitingHandler(HttpHandler delegate, int maxRequests) {
        this.delegate = delegate;
        this.capacity = maxRequests;
        this.refillPerNano = maxRequests / 60_000_000_000.0; // tokens per nanosecond, refills to `capacity` over 60s

        // Evict buckets idle for more than 5 minutes so the map doesn't grow forever.
        cleaner.scheduleAtFixedRate(this::evictStale, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        TokenBucket bucket = buckets.computeIfAbsent(clientKey, k -> new TokenBucket(capacity));

        if (bucket.tryConsume(capacity, refillPerNano)) {
            delegate.handle(exchange);
        } else {
            sendTooManyRequests(exchange);
        }
    }

    private String clientKey(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress() != null ? remote.getAddress().getHostAddress() : remote.toString();
    }

    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "Rate limit exceeded. Please slow down.".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Retry-After", "60");
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private void evictStale() {
        long now = System.nanoTime();
        long idleThresholdNanos = TimeUnit.MINUTES.toNanos(5);
        buckets.entrySet().removeIf(e -> (now - e.getValue().lastAccessNanos) > idleThresholdNanos);
    }

    /** Call when shutting down the server to release the cleanup thread. */
    public void shutdown() {
        cleaner.shutdownNow();
    }

    /**
     * Simple thread-safe token bucket. Tokens refill continuously based on
     * elapsed time, capped at `capacity`.
     */
    private static final class TokenBucket {
        private double tokens;
        private long lastRefillNanos;
        private volatile long lastAccessNanos;

        TokenBucket(double initialTokens) {
            this.tokens = initialTokens;
            this.lastRefillNanos = System.nanoTime();
            this.lastAccessNanos = lastRefillNanos;
        }

        synchronized boolean tryConsume(int capacity, double refillPerNano) {
            long now = System.nanoTime();
            long elapsed = now - lastRefillNanos;
            if (elapsed > 0) {
                tokens = Math.min(capacity, tokens + elapsed * refillPerNano);
                lastRefillNanos = now;
            }
            lastAccessNanos = now;

            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
    }
}
