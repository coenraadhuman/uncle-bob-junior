import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

/** Decorates a handler with per-client rate limiting; delegates when the client is within quota. */
public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int TOO_MANY_REQUESTS = 429;

    private final HttpHandler delegate;
    private final ClientRateLimiter rateLimiter;
    private final long retryAfterSeconds;

    public RateLimitingHttpHandler(HttpHandler delegate, ClientRateLimiter rateLimiter, Duration windowDuration) {
        this.delegate = Objects.requireNonNull(delegate);
        this.rateLimiter = Objects.requireNonNull(rateLimiter);
        this.retryAfterSeconds = windowDuration.toSeconds();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdFrom(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdFrom(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS, -1);
        exchange.close();
    }
}
