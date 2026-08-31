package com.example.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final byte[] RATE_LIMITED_BODY = "Too Many Requests".getBytes(StandardCharsets.UTF_8);

    private final HttpHandler delegate;
    private final FixedWindowRateLimiter rateLimiter;

    public RateLimitingHttpHandler(HttpHandler delegate, FixedWindowRateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdOf(exchange);
        if (!rateLimiter.allowRequest(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdOf(HttpExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRemoteAddress();
        return remoteAddress.getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        long retryAfterSeconds = rateLimiter.windowDuration().toSeconds();
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, RATE_LIMITED_BODY.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(RATE_LIMITED_BODY);
        }
    }
}
