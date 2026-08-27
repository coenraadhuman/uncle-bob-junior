package com.plg.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String RATE_LIMIT_MESSAGE = "Rate limit exceeded. Please slow down.";

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    private final Duration window;

    public RateLimitingHttpHandler(HttpHandler delegate, RateLimiter rateLimiter, Duration window) {
        this.delegate = Objects.requireNonNull(delegate);
        this.rateLimiter = Objects.requireNonNull(rateLimiter);
        this.window = Objects.requireNonNull(window);
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
        byte[] body = RATE_LIMIT_MESSAGE.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(RETRY_AFTER_HEADER, String.valueOf(window.toSeconds()));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
