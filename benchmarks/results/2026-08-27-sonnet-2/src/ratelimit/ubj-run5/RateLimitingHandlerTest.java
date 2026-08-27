// RateLimitingHandlerTest.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class RateLimitingHandlerTest {

    private static final InetSocketAddress CLIENT_ADDRESS =
            new InetSocketAddress("203.0.113.5", 54321);
    private static final Duration RETRY_AFTER = Duration.ofSeconds(30);

    @Test
    void delegatesWhenRequestIsAllowed() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimiter rateLimiter = mock(RateLimiter.class);
        when(rateLimiter.allowRequest(any())).thenReturn(true);
        HttpExchange exchange = exchangeFromClient(CLIENT_ADDRESS);

        new RateLimitingHandler(delegate, rateLimiter, RETRY_AFTER).handle(exchange);

        verify(delegate).handle(exchange);
        verify(exchange, never()).sendResponseHeaders(anyInt(), anyLong());
    }

    @Test
    void respondsWithTooManyRequestsWhenRateLimited() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimiter rateLimiter = mock(RateLimiter.class);
        when(rateLimiter.allowRequest(any())).thenReturn(false);
        HttpExchange exchange = exchangeFromClient(CLIENT_ADDRESS);

        new RateLimitingHandler(delegate, rateLimiter, RETRY_AFTER).handle(exchange);

        verify(delegate, never()).handle(any());
        verify(exchange).sendResponseHeaders(429, -1);
        verify(exchange).close();
    }

    private HttpExchange exchangeFromClient(InetSocketAddress address) {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(address);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        return exchange;
    }
}
