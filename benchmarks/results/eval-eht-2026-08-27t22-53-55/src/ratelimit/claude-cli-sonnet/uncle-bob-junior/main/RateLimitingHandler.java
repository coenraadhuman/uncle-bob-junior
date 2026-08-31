// RateLimitingHandler.java
package com.example.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

public final class RateLimitingHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    private final Clock clock;
    private final long retryAfterSeconds;

    public RateLimitingHandler(HttpHandler delegate, RateLimiter rateLimiter, Clock clock, Duration window) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
        this.retryAfterSeconds = window.toSeconds();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdFrom(exchange);
        if (!rateLimiter.tryAcquire(clientId, clock.instant())) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdFrom(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set(RETRY_AFTER_HEADER, String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, -1);
        exchange.close();
    }
}
