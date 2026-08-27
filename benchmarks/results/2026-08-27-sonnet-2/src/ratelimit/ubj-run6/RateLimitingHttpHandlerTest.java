// RateLimitingHttpHandlerTest.java
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
import static org.mockito.Mockito.*;

class RateLimitingHttpHandlerTest {

    private HttpExchange exchange;
    private HttpHandler delegate;
    private Headers responseHeaders;

    @BeforeEach
    void setUp() throws IOException {
        exchange = mock(HttpExchange.class);
        delegate = mock(HttpHandler.class);
        responseHeaders = new Headers();
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress("192.0.2.1", 5000));
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
    }

    @Test
    void passesThroughWhenUnderLimit() throws IOException {
        RateLimitingHttpHandler handler = handlerAllowing(true);
        handler.handle(exchange);
        verify(delegate).handle(exchange);
    }

    @Test
    void rejectsWithTooManyRequestsWhenOverLimit() throws IOException {
        RateLimitingHttpHandler handler = handlerAllowing(false);
        handler.handle(exchange);
        verify(delegate, never()).handle(exchange);
        verify(exchange).sendResponseHeaders(eq(429), anyLong());
        assertEquals("60", responseHeaders.getFirst("Retry-After"));
    }

    private RateLimitingHttpHandler handlerAllowing(boolean allowed) {
        RateLimiter stubLimiter = clientId -> allowed;
        return new RateLimitingHttpHandler(delegate, stubLimiter, Duration.ofSeconds(60));
    }
}
