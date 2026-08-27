import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;

final class RateLimitingHttpHandler implements HttpHandler, Closeable {

    private static final int DEFAULT_MAX_REQUESTS_PER_WINDOW = 5;
    private static final Duration DEFAULT_WINDOW = Duration.ofMinutes(1);
    private static final int TOO_MANY_REQUESTS_STATUS_CODE = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final byte[] REJECTION_BODY = "Too Many Requests".getBytes();

    private final HttpHandler delegate;
    private final ClientRateLimiterRegistry rateLimiterRegistry;
    private final int retryAfterSeconds;

    RateLimitingHttpHandler(HttpHandler delegate, int maxRequestsPerWindow, Duration window) {
        this.delegate = delegate;
        this.rateLimiterRegistry = new ClientRateLimiterRegistry(maxRequestsPerWindow, window, Clock.systemUTC());
        this.retryAfterSeconds = (int) window.getSeconds();
    }

    static RateLimitingHttpHandler withDefaults(HttpHandler delegate) {
        return new RateLimitingHttpHandler(delegate, DEFAULT_MAX_REQUESTS_PER_WINDOW, DEFAULT_WINDOW);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = resolveClientKey(exchange);
        if (!rateLimiterRegistry.tryAcquire(clientKey)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    @Override
    public void close() {
        rateLimiterRegistry.close();
    }

    private String resolveClientKey(HttpExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRemoteAddress();
        return remoteAddress.getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS_STATUS_CODE, REJECTION_BODY.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(REJECTION_BODY);
        }
    }
}
