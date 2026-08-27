package ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RateLimitingHandlerTest {

    private static final int LIMIT = 2;
    private static final int HTTP_OK = 200;
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    private final HttpHandler okDelegate = exchange -> exchange.sendResponseHeaders(HTTP_OK, -1);
    private final RateLimitingHandler handler = new RateLimitingHandler(
            okDelegate, new SlidingWindowRateLimiter(LIMIT, Duration.ofMinutes(1)), FIXED_CLOCK);

    @Test
    void delegatesWhileClientIsUnderTheLimit() throws IOException {
        RecordingExchange exchange = new RecordingExchange("203.0.113.7");
        handler.handle(exchange);
        assertEquals(HTTP_OK, exchange.getResponseCode());
    }

    @Test
    void answers429WithRetryAfterOnceOverTheLimit() throws IOException {
        for (int i = 0; i < LIMIT; i++) {
            handler.handle(new RecordingExchange("203.0.113.7"));
        }
        RecordingExchange exchange = new RecordingExchange("203.0.113.7");
        handler.handle(exchange);

        assertEquals(RateLimitingHandler.HTTP_TOO_MANY_REQUESTS, exchange.getResponseCode());
        assertEquals("60", exchange.getResponseHeaders()
                .getFirst(RateLimitingHandler.RETRY_AFTER_HEADER));
    }

    @Test
    void doesNotPenaliseOtherClients() throws IOException {
        for (int i = 0; i < LIMIT; i++) {
            handler.handle(new RecordingExchange("203.0.113.7"));
        }
        RecordingExchange otherClient = new RecordingExchange("198.51.100.9");
        handler.handle(otherClient);
        assertEquals(HTTP_OK, otherClient.getResponseCode());
    }

    /** Minimal in-memory HttpExchange so tests need no sockets or mocking framework. */
    private static final class RecordingExchange extends HttpExchange {
        private static final int CLIENT_PORT = 40000;

        private final InetSocketAddress remoteAddress;
        private final Headers responseHeaders = new Headers();
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        private int responseCode = -1;

        RecordingExchange(String clientIp) {
            this.remoteAddress = new InetSocketAddress(clientIp, CLIENT_PORT);
        }

        @Override public InetSocketAddress getRemoteAddress() { return remoteAddress; }
        @Override public Headers getResponseHeaders() { return responseHeaders; }
        @Override public int getResponseCode() { return responseCode; }
        @Override public OutputStream getResponseBody() { return responseBody; }
        @Override public void sendResponseHeaders(int code, long length) { this.responseCode = code; }

        @Override public Headers getRequestHeaders() { return new Headers(); }
        @Override public URI getRequestURI() { return URI.create("/"); }
        @Override public String getRequestMethod() { return "GET"; }
        @Override public HttpContext getHttpContext() { return null; }
        @Override public void close() {}
        @Override public InputStream getRequestBody() { return new ByteArrayInputStream(new byte[0]); }
        @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress(0); }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {}
        @Override public void setStreams(InputStream in, OutputStream out) {}
        @Override public HttpPrincipal getPrincipal() { return null; }
    }
}
