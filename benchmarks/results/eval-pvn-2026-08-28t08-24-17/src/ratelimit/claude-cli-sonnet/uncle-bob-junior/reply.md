Assumptions: using `com.sun.net.httpserver.HttpHandler`/`HttpExchange` (JDK's built-in HTTP server), identifying clients by remote IP, and treating "a few" as 5 requests per 60-second window (all configurable via named constants).

```java
package ratelimit;

public interface ClientRateLimiter {
    boolean allowRequest(String clientId);
}
```

```java
package ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public final class FixedWindowRateLimiter implements ClientRateLimiter {

    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, RequestWindow> windowsByClient = new ConcurrentHashMap<>();
    private final Clock clock;

    public FixedWindowRateLimiter() {
        this(Clock.systemUTC());
    }

    public FixedWindowRateLimiter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean allowRequest(String clientId) {
        Instant now = clock.instant();
        RequestWindow updatedWindow = windowsByClient.compute(clientId,
                (id, existingWindow) -> nextWindow(existingWindow, now));
        return updatedWindow.requestCount() <= MAX_REQUESTS_PER_WINDOW;
    }

    private RequestWindow nextWindow(RequestWindow existingWindow, Instant now) {
        if (existingWindow == null || existingWindow.hasExpired(now, WINDOW_DURATION)) {
            return new RequestWindow(now, 1);
        }
        return existingWindow.incremented();
    }

    private record RequestWindow(Instant startedAt, int requestCount) {

        boolean hasExpired(Instant now, Duration windowDuration) {
            return now.isAfter(startedAt.plus(windowDuration));
        }

        RequestWindow incremented() {
            return new RequestWindow(startedAt, requestCount + 1);
        }
    }
}
```

```java
package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int TOO_MANY_REQUESTS = 429;
    private static final int NO_RESPONSE_BODY = -1;

    private final HttpHandler delegate;
    private final ClientRateLimiter rateLimiter;

    public RateLimitingHttpHandler(HttpHandler delegate, ClientRateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdOf(exchange);
        if (!rateLimiter.allowRequest(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdOf(HttpExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRemoteAddress();
        return remoteAddress.getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS, NO_RESPONSE_BODY);
        exchange.close();
    }
}
```

```java
package ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowRateLimiterTest {

    private static final String CLIENT_A = "client-a";
    private static final String CLIENT_B = "client-b";
    private static final int MAX_REQUESTS_PER_WINDOW = 5;

    private final MutableTestClock clock = new MutableTestClock(Instant.parse("2026-08-28T10:00:00Z"));
    private final FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(clock);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            assertTrue(rateLimiter.allowRequest(CLIENT_A));
        }
    }

    @Test
    void blocksRequestsBeyondTheLimitWithinTheSameWindow() {
        exhaustQuotaFor(CLIENT_A);

        assertFalse(rateLimiter.allowRequest(CLIENT_A));
    }

    @Test
    void tracksEachClientIndependently() {
        exhaustQuotaFor(CLIENT_A);

        assertTrue(rateLimiter.allowRequest(CLIENT_B));
    }

    @Test
    void allowsRequestsAgainAfterTheWindowElapses() {
        exhaustQuotaFor(CLIENT_A);
        assertFalse(rateLimiter.allowRequest(CLIENT_A));

        clock.advanceBy(Duration.ofMinutes(1).plusSeconds(1));

        assertTrue(rateLimiter.allowRequest(CLIENT_A));
    }

    private void exhaustQuotaFor(String clientId) {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            rateLimiter.allowRequest(clientId);
        }
    }

    private static final class MutableTestClock extends Clock {
        private Instant currentInstant;

        MutableTestClock(Instant startingInstant) {
            this.currentInstant = startingInstant;
        }

        void advanceBy(Duration duration) {
            currentInstant = currentInstant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException("not needed for tests");
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }
    }
}
```

```java
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
```

Cleaned: rate-limiting logic isolated behind `ClientRateLimiter` (one seam, swappable algorithm/store), window state is an immutable record, constants named. Safe to change because the handler never touches counting logic directly and each class has one job, tested independently.

Note: the client identifier is the remote IP, which is personal data under GDPR even held only in memory. If requests pass through a load balancer/proxy, confirm with Infra/DPO whether `X-Forwarded-For` should be trusted before using it as the rate-limit key.