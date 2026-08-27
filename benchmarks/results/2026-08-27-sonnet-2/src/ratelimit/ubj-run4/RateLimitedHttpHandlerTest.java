// RateLimitedHttpHandlerTest.java
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitedHttpHandlerTest {

    private static final String CLIENT_IP = "192.0.2.1";

    private HttpHandler delegate;
    private RateLimiter rateLimiter;
    private RateLimitedHttpHandler handler;
    private HttpExchange exchange;

    @BeforeEach
    void setUp() throws IOException {
        delegate = mock(HttpHandler.class);
        rateLimiter = mock(RateLimiter.class);
        handler = new RateLimitedHttpHandler(delegate, rateLimiter, Duration.ofMinutes(1));

        exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress(CLIENT_IP, 54321));
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
    }

    @Test
    void delegatesWhenWithinLimit() throws IOException {
        when(rateLimiter.tryAcquire(CLIENT_IP)).thenReturn(true);

        handler.handle(exchange);

        verify(delegate).handle(exchange);
    }

    @Test
    void rejectsWithTooManyRequestsWhenLimitExceeded() throws IOException {
        when(rateLimiter.tryAcquire(CLIENT_IP)).thenReturn(false);

        handler.handle(exchange);

        verify(delegate, never()).handle(exchange);
        verify(exchange).sendResponseHeaders(eq(429), anyLong());
        assertEquals("60", exchange.getResponseHeaders().getFirst("Retry-After"));
    }
}
