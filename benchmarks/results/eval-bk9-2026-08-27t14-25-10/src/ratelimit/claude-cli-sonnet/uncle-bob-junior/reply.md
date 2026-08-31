Assumptions: using the JDK's built-in `com.sun.net.httpserver.HttpHandler`/`HttpServer` (no external framework specified); "client" = remote IP address; limit set to 5 requests per minute per client using a fixed-window counter (simple, thread-safe, no extra dependencies); exceeding the limit returns `429 Too Many Requests` with a `Retry-After` header; tests use JUnit 5, and the handler test uses Mockito to stand in for `HttpExchange` (mocking it by hand requires implementing ~20 abstract methods). Known limitation, not implemented since it wasn't asked for: the client map grows with distinct IPs over the server's lifetime — add idle-entry eviction before using this in production with untrusted traffic.

```java
package com.example.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public final class FixedWindowRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration windowDuration;
    private final Clock clock;
    private final ConcurrentHashMap<String, ClientWindow> windowsByClientId = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDuration = windowDuration;
        this.clock = clock;
    }

    public boolean allowRequest(String clientId) {
        Instant now = clock.instant();
        ClientWindow updatedWindow = windowsByClientId.compute(clientId,
                (id, currentWindow) -> nextWindow(currentWindow, now));
        return updatedWindow.requestCount() <= maxRequestsPerWindow;
    }

    public Duration windowDuration() {
        return windowDuration;
    }

    private ClientWindow nextWindow(ClientWindow currentWindow, Instant now) {
        if (currentWindow == null || isExpired(currentWindow, now)) {
            return new ClientWindow(now, 1);
        }
        return currentWindow.incremented();
    }

    private boolean isExpired(ClientWindow window, Instant now) {
        return now.isAfter(window.startedAt().plus(windowDuration));
    }

    private record ClientWindow(Instant startedAt, int requestCount) {
        ClientWindow incremented() {
            return new ClientWindow(startedAt, requestCount + 1);
        }
    }
}
```

```java
package com.example.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final byte[] RATE_LIMITED_BODY = "Too Many Requests".getBytes(StandardCharsets.UTF_8);

    private final HttpHandler delegate;
    private final FixedWindowRateLimiter rateLimiter;

    public RateLimitingHttpHandler(HttpHandler delegate, FixedWindowRateLimiter rateLimiter) {
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
        long retryAfterSeconds = rateLimiter.windowDuration().toSeconds();
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, RATE_LIMITED_BODY.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(RATE_LIMITED_BODY);
        }
    }
}
```

```java
package com.example.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

public final class RateLimitedServerExample {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(
                MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1), Clock.systemUTC());

        HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
        server.createContext("/", new RateLimitingHttpHandler(
                RateLimitedServerExample::handleHello, rateLimiter));
        server.start();
    }

    private static void handleHello(HttpExchange exchange) throws IOException {
        byte[] body = "Hello, world!".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
```

```java
package com.example.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowRateLimiterTest {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT_A = "client-a";
    private static final String CLIENT_B = "client-b";

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private final FixedWindowRateLimiter rateLimiter =
            new FixedWindowRateLimiter(MAX_REQUESTS, WINDOW, clock);

    @Test
    void allowsRequestsUpToTheLimitWithinAWindow() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.allowRequest(CLIENT_A));
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimitWithinAWindow() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.allowRequest(CLIENT_A);
        }

        assertFalse(rateLimiter.allowRequest(CLIENT_A));
    }

    @Test
    void resetsTheLimitOnceTheWindowExpires() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.allowRequest(CLIENT_A);
        }
        clock.advanceBy(WINDOW.plusSeconds(1));

        assertTrue(rateLimiter.allowRequest(CLIENT_A));
    }

    @Test
    void tracksEachClientIndependently() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.allowRequest(CLIENT_A);
        }

        assertTrue(rateLimiter.allowRequest(CLIENT_B));
    }

    private static final class MutableClock extends Clock {
        private Instant currentInstant;

        MutableClock(Instant startingInstant) {
            this.currentInstant = startingInstant;
        }

        void advanceBy(Duration duration) {
            currentInstant = currentInstant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }
    }
}
```

```java
package com.example.ratelimit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitingHttpHandlerTest {

    private static final String CLIENT_IP = "203.0.113.5";

    @Test
    void delegatesRequestWhenUnderLimit() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        FixedWindowRateLimiter rateLimiter =
                new FixedWindowRateLimiter(1, Duration.ofMinutes(1), Clock.systemUTC());
        RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, rateLimiter);
        HttpExchange exchange = exchangeFromClient(CLIENT_IP);

        handler.handle(exchange);

        verify(delegate).handle(exchange);
    }

    @Test
    void rejectsRequestWhenOverLimit() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        FixedWindowRateLimiter rateLimiter =
                new FixedWindowRateLimiter(1, Duration.ofMinutes(1), Clock.systemUTC());
        RateLimitingHttpHandler handler = new RateLimitingHttpHandler(delegate, rateLimiter);
        handler.handle(exchangeFromClient(CLIENT_IP));
        HttpExchange secondExchange = exchangeFromClient(CLIENT_IP);

        handler.handle(secondExchange);

        verify(delegate, never()).handle(secondExchange);
        verify(secondExchange).sendResponseHeaders(eq(429), anyLong());
    }

    private HttpExchange exchangeFromClient(String ip) {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress(ip, 12345));
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        return exchange;
    }
}
```

Cleaned: rate-limiting policy isolated in `FixedWindowRateLimiter` (pure, clock-injected, no magic numbers) separate from the HTTP plumbing in `RateLimitingHttpHandler` → safe to change the algorithm (e.g. swap fixed-window for token-bucket) without touching the handler, and safe to add new handlers by wrapping them the same way.