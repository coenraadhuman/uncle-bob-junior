// RateLimitingHttpHandler.java
package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Wraps a delegate handler and rejects clients that exceed the configured rate limit. */
public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String RATE_LIMIT_MESSAGE = "Rate limit exceeded. Try again later.";

    private final HttpHandler delegate;
    private final FixedWindowRateLimiter rateLimiter;

    public RateLimitingHttpHandler(HttpHandler delegate, FixedWindowRateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdentifier(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            sendTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdentifier(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = RATE_LIMIT_MESSAGE.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, String.valueOf(rateLimiter.windowDurationSeconds()));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
