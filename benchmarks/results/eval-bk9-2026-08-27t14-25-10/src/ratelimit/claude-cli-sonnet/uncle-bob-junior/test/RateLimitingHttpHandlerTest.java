package com.example.ratelimit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitingHttpHandlerTest {

    private static final String CLIENT_IP = "203.0.113.5";

    @Test
    void delegatesRequestWhenUnderLimit() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        FixedWindowRateLimiter rateLimiter =
                new FixedWindowRateLimiter(1, Duration.ofMinutes(1), Clock.systemUTC());
        RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, rateLimiter);
        HttpExchange exchange = exchangeFromClient(CLIENT_IP);

        handler.handle(exchange);

        verify(delegate).handle(exchange);
    }

    @Test
    void rejectsRequestWhenOverLimit() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        FixedWindowRateLimiter rateLimiter =
                new FixedWindowRateLimiter(1, Duration.ofMinutes(1), Clock.systemUTC());
        RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, rateLimiter);
        handler.handle(exchangeFromClient(CLIENT_IP));
        HttpExchange secondExchange = exchangeFromClient(CLIENT_IP);

        handler.handle(secondExchange);

        verify(delegate, never()).handle(secondExchange);
        verify(secondExchange).sendResponseHeaders(eq(429), anyLong());
    }

    private HttpExchange exchangeFromClient(String ip) {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress(ip, 12345));
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        return exchange;
    }
}
