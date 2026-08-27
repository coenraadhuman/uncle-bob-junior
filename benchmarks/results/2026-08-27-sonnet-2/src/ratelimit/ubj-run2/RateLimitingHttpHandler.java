// RateLimitingHttpHandler.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Wraps a delegate handler and rejects clients that exceed the configured
 * rate with 429 Too Many Requests.
 */
public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String RETRY_AFTER_SECONDS = "60";
    private static final byte[] RATE_LIMITED_BODY =
            "Too Many Requests".getBytes(StandardCharsets.UTF_8);

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;

    public RateLimitingHttpHandler(HttpHandler delegate, RateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!rateLimiter.tryAcquire(clientIdOf(exchange))) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdOf(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, RETRY_AFTER_SECONDS);
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS, RATE_LIMITED_BODY.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(RATE_LIMITED_BODY);
        }
    }
}
