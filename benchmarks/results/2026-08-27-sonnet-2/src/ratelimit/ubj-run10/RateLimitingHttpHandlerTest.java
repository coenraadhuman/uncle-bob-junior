import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

final class RateLimitingHttpHandlerTest {

    private HttpExchange newExchangeMock() {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress("192.0.2.1", 54321));
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        return exchange;
    }

    @Test
    void allowsRequestsWithinLimitToReachDelegate() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        try (RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, 2, Duration.ofMinutes(1))) {
            handler.handle(newExchangeMock());
            handler.handle(newExchangeMock());

            verify(delegate, times(2)).handle(any());
        }
    }

    @Test
    void rejectsRequestsBeyondLimitWithoutCallingDelegate() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        try (RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, 1, Duration.ofMinutes(1))) {
            handler.handle(newExchangeMock());

            HttpExchange rejected = newExchangeMock();
            handler.handle(rejected);

            verify(delegate, times(1)).handle(any());
            verify(rejected).sendResponseHeaders(eq(429), anyLong());
        }
    }
}
