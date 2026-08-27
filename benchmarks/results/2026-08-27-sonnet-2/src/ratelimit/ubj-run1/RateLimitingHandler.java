// File: RateLimitingHandler.java
package com.postcodeloterij.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/** Wraps a delegate handler and rejects requests once a client exceeds its rate limit. */
public final class RateLimitingHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final byte[] RATE_LIMIT_MESSAGE =
            "Too Many Requests".getBytes(StandardCharsets.UTF_8);
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String RETRY_AFTER_SECONDS = "60";

    private final HttpHandler delegate;
    private final TokenBucketRateLimiter rateLimiter;

    public RateLimitingHandler(HttpHandler delegate, TokenBucketRateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!rateLimiter.tryAcquire(clientKeyOf(exchange))) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientKeyOf(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, RETRY_AFTER_SECONDS);
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, RATE_LIMIT_MESSAGE.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(RATE_LIMIT_MESSAGE);
        }
    }
}
