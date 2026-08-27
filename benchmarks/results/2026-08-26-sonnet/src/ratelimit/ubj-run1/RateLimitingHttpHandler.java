package com.example.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Decorates an HttpHandler with per-client rate limiting.
 */
public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int TOO_MANY_REQUESTS_STATUS_CODE = 429;

    private final HttpHandler delegateHandler;
    private final RateLimiter rateLimiter;

    public RateLimitingHttpHandler(HttpHandler delegateHandler, RateLimiter rateLimiter) {
        this.delegateHandler = delegateHandler;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = resolveClientKey(exchange);
        if (!rateLimiter.isRequestAllowed(clientKey)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegateHandler.handle(exchange);
    }

    private String resolveClientKey(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] responseBody = "429 Too Many Requests\n".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(rateLimiter.windowDuration().toSeconds()));
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS_STATUS_CODE, responseBody.length);
        try (OutputStream responseStream = exchange.getResponseBody()) {
            responseStream.write(responseBody);
        }
    }
}
