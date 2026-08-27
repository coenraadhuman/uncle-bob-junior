import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
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
import org.junit.jupiter.api.Test;

class RateLimitedHttpHandlerTest {

    private static final int LIMIT = 2;
    private static final int OK = 200;
    private static final int TOO_MANY_REQUESTS = 429;

    private final SlidingWindowRateLimiter limiter =
            new SlidingWindowRateLimiter(LIMIT, Duration.ofMinutes(1), Clock.systemUTC());
    private final RateLimitedHttpHandler handler =
            new RateLimitedHttpHandler(RateLimitedHttpHandlerTest::respondOk, limiter);

    @Test
    void delegatesWhileWithinTheLimit() throws IOException {
        StubHttpExchange exchange = new StubHttpExchange("203.0.113.10");
        handler.handle(exchange);
        assertEquals(OK, exchange.getResponseCode());
    }

    @Test
    void rejectsWithRetryAfterOnceOverTheLimit() throws IOException {
        for (int i = 0; i < LIMIT; i++) {
            handler.handle(new StubHttpExchange("203.0.113.10"));
        }
        StubHttpExchange rejected = new StubHttpExchange("203.0.113.10");
        handler.handle(rejected);

        assertEquals(TOO_MANY_REQUESTS, rejected.getResponseCode());
        assertEquals("60", rejected.getResponseHeaders().getFirst("Retry-After"));
    }

    private static void respondOk(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(OK, -1);
    }

    /** Minimal in-memory HttpExchange: records the response, no real socket. */
    private static final class StubHttpExchange extends HttpExchange {
        private final InetSocketAddress remoteAddress;
        private final Headers responseHeaders = new Headers();
        private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        private int responseCode = -1;

        StubHttpExchange(String clientIp) {
            this.remoteAddress = new InetSocketAddress(clientIp, 54321);
        }

        @Override public void sendResponseHeaders(int code, long length) { responseCode = code; }
        @Override public int getResponseCode() { return responseCode; }
        @Override public Headers getResponseHeaders() { return responseHeaders; }
        @Override public OutputStream getResponseBody() { return responseBody; }
        @Override public InetSocketAddress getRemoteAddress() { return remoteAddress; }
        @Override public Headers getRequestHeaders() { return new Headers(); }
        @Override public InputStream getRequestBody() { return new ByteArrayInputStream(new byte[0]); }
        @Override public URI getRequestURI() { return URI.create("/"); }
        @Override public String getRequestMethod() { return "GET"; }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public InetSocketAddress getLocalAddress() { return new InetSocketAddress(8080); }
        @Override public HttpContext getHttpContext() { return null; }
        @Override public HttpPrincipal getPrincipal() { return null; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) { }
        @Override public void setStreams(InputStream in, OutputStream out) { }
        @Override public void close() { }
    }
}
