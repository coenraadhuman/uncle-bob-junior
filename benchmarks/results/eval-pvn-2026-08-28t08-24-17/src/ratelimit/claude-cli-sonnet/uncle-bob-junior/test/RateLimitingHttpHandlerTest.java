package ratelimit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpPrincipal;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitingHttpHandlerTest {

    private static final int TOO_MANY_REQUESTS = 429;

    @Test
    void delegatesWhenRequestIsWithinTheLimit() throws IOException {
        RecordingHandler delegate = new RecordingHandler();
        RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, alwaysAllow());

        handler.handle(new FakeHttpExchange());

        assertTrue(delegate.wasInvoked);
    }

    @Test
    void rejectsWithTooManyRequestsWhenLimitExceeded() throws IOException {
        RecordingHandler delegate = new RecordingHandler();
        RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, alwaysReject());
        FakeHttpExchange exchange = new FakeHttpExchange();

        handler.handle(exchange);

        assertFalse(delegate.wasInvoked);
        assertEquals(TOO_MANY_REQUESTS, exchange.sentResponseCode);
    }

    private static ClientRateLimiter alwaysAllow() {
        return clientId -> true;
    }

    private static ClientRateLimiter alwaysReject() {
        return clientId -> false;
    }

    private static final class RecordingHandler implements HttpHandler {
        boolean wasInvoked = false;

        @Override
        public void handle(HttpExchange exchange) {
            wasInvoked = true;
        }
    }

    private static final class FakeHttpExchange extends HttpExchange {
        int sentResponseCode = -1;

        @Override
        public InetSocketAddress getRemoteAddress() {
            return new InetSocketAddress("203.0.113.5", 54321);
        }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) {
            sentResponseCode = rCode;
        }

        @Override
        public OutputStream getResponseBody() {
            return new ByteArrayOutputStream();
        }

        @Override
        public void close() {
        }

        @Override
        public Headers getRequestHeaders() {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public Headers getResponseHeaders() {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public URI getRequestURI() {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public String getRequestMethod() {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public HttpContext getHttpContext() {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public InputStream getRequestBody() {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public int getResponseCode() {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public String getProtocol() {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public Object getAttribute(String name) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public void setAttribute(String name, Object value) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public void setStreams(InputStream i, OutputStream o) {
            throw new UnsupportedOperationException("not needed for this test");
        }

        @Override
        public HttpPrincipal getPrincipal() {
            throw new UnsupportedOperationException("not needed for this test");
        }
    }
}
