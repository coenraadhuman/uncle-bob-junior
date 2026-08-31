// RateLimitingHttpHandlerTest.java
package ratelimit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimitingHttpHandlerTest {

    private static final int MAX_REQUESTS = 2;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    @Test
    void forwardsRequestToDelegateWhenUnderTheLimit() throws Exception {
        RecordingHandler delegate = new RecordingHandler();
        try (FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(MAX_REQUESTS, Duration.ofMinutes(1))) {
            RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, limiter);
            handler.handle(exchangeFrom("10.0.0.1", new ByteArrayOutputStream()));
        }
        assertTrue(delegate.wasInvoked);
    }

    @Test
    void rejectsRequestsOnceLimitIsExceeded() throws Exception {
        RecordingHandler delegate = new RecordingHandler();
        try (FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(MAX_REQUESTS, Duration.ofMinutes(1))) {
            RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, limiter);
            for (int i = 0; i < MAX_REQUESTS; i++) {
                handler.handle(exchangeFrom("10.0.0.2", new ByteArrayOutputStream()));
            }
            delegate.wasInvoked = false;

            HttpExchange exchange = exchangeFrom("10.0.0.2", new ByteArrayOutputStream());
            handler.handle(exchange);

            assertFalse(delegate.wasInvoked);
            org.mockito.Mockito.verify(exchange).sendResponseHeaders(eq(HTTP_TOO_MANY_REQUESTS), anyInt());
        }
    }

    private HttpExchange exchangeFrom(String remoteIp, ByteArrayOutputStream responseBody) throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress())
                .thenReturn(new InetSocketAddress(InetAddress.getByName(remoteIp), 0));
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(responseBody);
        doNothing().when(exchange).sendResponseHeaders(anyInt(), anyInt());
        return exchange;
    }

    private static final class RecordingHandler implements HttpHandler {
        boolean wasInvoked;

        @Override
        public void handle(HttpExchange exchange) {
            wasInvoked = true;
        }
    }
}
