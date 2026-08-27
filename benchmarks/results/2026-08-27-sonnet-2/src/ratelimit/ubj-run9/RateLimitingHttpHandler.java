// File: RateLimitingHttpHandler.java
package com.example.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Wraps an {@link HttpHandler}, rejecting requests from clients that have
 * exceeded the configured rate limit with a {@code 429 Too Many Requests}
 * response.
 */
public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final byte[] RATE_LIMIT_EXCEEDED_BODY =
            "Rate limit exceeded. Please slow down.".getBytes(StandardCharsets.UTF_8);

    private final HttpHandler delegate;
    private final FixedWindowRateLimiter rateLimiter;
    private final long retryAfterSeconds;

    public RateLimitingHttpHandler(HttpHandler delegate, FixedWindowRateLimiter rateLimiter, Duration windowDuration) {
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
        InetSocketAddress remoteAddress = exchange.getRemoteAddress();
        return remoteAddress.getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, RATE_LIMIT_EXCEEDED_BODY.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(RATE_LIMIT_EXCEEDED_BODY);
        }
    }
}
