package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.Duration;

/**
 * Wraps a delegate {@link HttpHandler}, rejecting requests from clients who
 * have exceeded {@code rateLimiter}'s configured limit with HTTP 429.
 */
public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int NO_RESPONSE_BODY = -1;
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    private final long retryAfterSeconds;

    public RateLimitingHttpHandler(HttpHandler delegate, RateLimiter rateLimiter, Duration windowDuration) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        this.retryAfterSeconds = windowDuration.toSeconds();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = extractClientId(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private static String extractClientId(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, NO_RESPONSE_BODY);
        exchange.close();
    }
}
