package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * Wraps a delegate {@link HttpHandler} and rejects requests from a client once it
 * exceeds the configured {@link RateLimiter}, responding with 429 and a Retry-After header.
 */
public final class RateLimitingHandler implements HttpHandler {

    private static final int TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final int NO_RESPONSE_BODY = -1;

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    private final long retryAfterSeconds;

    public RateLimitingHandler(HttpHandler delegate, RateLimiter rateLimiter, long retryAfterSeconds) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdOf(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdOf(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS, NO_RESPONSE_BODY);
        exchange.close();
    }
}
