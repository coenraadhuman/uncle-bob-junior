package com.plg.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Wraps a delegate handler and rejects requests over the configured
 * per-client rate with HTTP 429.
 */
public final class RateLimitedHttpHandler implements HttpHandler {

    private static final int TOO_MANY_REQUESTS = 429;
    private static final byte[] RATE_LIMITED_BODY =
            "Rate limit exceeded. Please slow down.".getBytes(StandardCharsets.UTF_8);

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;

    public RateLimitedHttpHandler(HttpHandler delegate, RateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = exchange.getRemoteAddress().getAddress().getHostAddress();

        if (!rateLimiter.tryAcquire(clientKey)) {
            rejectWithTooManyRequests(exchange);
            return;
        }

        delegate.handle(exchange);
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS, RATE_LIMITED_BODY.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(RATE_LIMITED_BODY);
        }
    }
}
