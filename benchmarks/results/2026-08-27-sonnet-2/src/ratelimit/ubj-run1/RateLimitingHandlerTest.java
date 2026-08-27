// File: RateLimitingHandlerTest.java
package com.postcodeloterij.ratelimit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitingHandlerTest {

    private static final int MAX_REQUESTS_PER_MINUTE = 1;
    private static final InetSocketAddress CLIENT_ADDRESS = new InetSocketAddress("192.0.2.10", 5555);

    @Test
    void forwardsRequestToDelegateWhenWithinLimit() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimitingHandler handler = new RateLimitingHandler(delegate,
                new TokenBucketRateLimiter(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
        HttpExchange exchange = exchangeFrom(CLIENT_ADDRESS);

        handler.handle(exchange);

        verify(delegate).handle(exchange);
    }

    @Test
    void rejectsWithTooManyRequestsWhenLimitExceeded() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimitingHandler handler = new RateLimitingHandler(delegate,
                new TokenBucketRateLimiter(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)));
        handler.handle(exchangeFrom(CLIENT_ADDRESS));
        HttpExchange secondExchange = exchangeFrom(CLIENT_ADDRESS);

        handler.handle(secondExchange);

        verify(delegate, times(1)).handle(any());
        verify(secondExchange).sendResponseHeaders(eq(429), anyLong());
    }

    private HttpExchange exchangeFrom(InetSocketAddress clientAddress) throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(clientAddress);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        return exchange;
    }
}
