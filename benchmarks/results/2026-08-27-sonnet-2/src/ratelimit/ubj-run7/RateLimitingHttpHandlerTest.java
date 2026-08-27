package com.plg.ratelimit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RateLimitingHttpHandlerTest {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    @Test
    void forwardsRequestWhenWithinLimit() throws IOException {
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, alwaysAllow(), WINDOW);
        HttpExchange exchange = fakeExchangeFrom("10.0.0.1");

        handler.handle(exchange);

        verify(delegate, times(1)).handle(exchange);
    }

    @Test
    void rejectsRequestWithTooManyRequestsWhenLimitExceeded() throws IOException {
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, alwaysReject(), WINDOW);
        HttpExchange exchange = fakeExchangeFrom("10.0.0.2");

        handler.handle(exchange);

        verify(delegate, never()).handle(exchange);
        verify(exchange).sendResponseHeaders(eq(429), anyLong());
        assertEquals("60", exchange.getResponseHeaders().getFirst("Retry-After"));
    }

    private static RateLimiter alwaysAllow() {
        return clientId -> true;
    }

    private static RateLimiter alwaysReject() {
        return clientId -> false;
    }

    private static HttpExchange fakeExchangeFrom(String ipAddress) throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress(ipAddress, 5555));
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        return exchange;
    }
}
