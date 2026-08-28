// RateLimitingHandlerTest.java
package com.example.ratelimit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RateLimitingHandlerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-08-28T10:00:00Z"), ZoneOffset.UTC);
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT_IP = "127.0.0.1";

    @Test
    void forwardsRequestWhenUnderLimit() throws IOException {
        HttpExchange exchange = exchangeFrom(CLIENT_IP);
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimiter rateLimiter = mock(RateLimiter.class);
        when(rateLimiter.tryAcquire(eq(CLIENT_IP), any())).thenReturn(true);

        new RateLimitingHandler(delegate, rateLimiter, FIXED_CLOCK, WINDOW).handle(exchange);

        verify(delegate).handle(exchange);
        verify(exchange, never()).sendResponseHeaders(eq(429), anyLong());
    }

    @Test
    void rejectsRequestWhenOverLimit() throws IOException {
        HttpExchange exchange = exchangeFrom(CLIENT_IP);
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimiter rateLimiter = mock(RateLimiter.class);
        when(rateLimiter.tryAcquire(eq(CLIENT_IP), any())).thenReturn(false);

        new RateLimitingHandler(delegate, rateLimiter, FIXED_CLOCK, WINDOW).handle(exchange);

        verify(delegate, never()).handle(any());
        verify(exchange).sendResponseHeaders(429, -1);
    }

    private HttpExchange exchangeFrom(String clientIp) {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress(clientIp, 1234));
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        return exchange;
    }
}
