Assumptions: this wraps the JDK's built-in `com.sun.net.httpserver.HttpHandler`, keys clients by remote IP, uses an in-memory sliding window (single-node; for a multi-node deployment use Bucket4j or a gateway limiter instead), and defaults to 5 requests per minute. Tests use JUnit 5.

```java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Sliding-window rate limiter: at most {@code maxRequestsPerWindow} calls
 * per client within any rolling {@code window}. Thread-safe.
 */
public final class SlidingWindowRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Clock clock;
    // ubj: idle-client entries are never evicted; add scheduled cleanup if client cardinality grows large
    private final ConcurrentMap<String, Deque<Instant>> requestTimesByClient = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration window, Clock clock) {
        if (maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be positive");
        }
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
        this.clock = clock;
    }

    /** Returns true and records the request if the client is under its limit. */
    public boolean tryAcquire(String clientId) {
        Deque<Instant> requestTimes =
                requestTimesByClient.computeIfAbsent(clientId, id -> new ArrayDeque<>());
        synchronized (requestTimes) {
            evictOlderThanWindow(requestTimes);
            if (requestTimes.size() >= maxRequestsPerWindow) {
                return false;
            }
            requestTimes.addLast(clock.instant());
            return true;
        }
    }

    private void evictOlderThanWindow(Deque<Instant> requestTimes) {
        Instant windowStart = clock.instant().minus(window);
        while (!requestTimes.isEmpty() && requestTimes.peekFirst().isBefore(windowStart)) {
            requestTimes.removeFirst();
        }
    }

    long windowSeconds() {
        return window.toSeconds();
    }
}
```

```java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

/**
 * Wraps any HttpHandler and answers 429 Too Many Requests once a client
 * exceeds its per-window limit. Keys clients by remote IP address.
 */
public final class RateLimitingHandler implements HttpHandler {

    public static final int DEFAULT_MAX_REQUESTS_PER_MINUTE = 5;
    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);

    private final SlidingWindowRateLimiter rateLimiter;
    private final HttpHandler delegate;

    public RateLimitingHandler(SlidingWindowRateLimiter rateLimiter, HttpHandler delegate) {
        this.rateLimiter = rateLimiter;
        this.delegate = delegate;
    }

    public static RateLimitingHandler withDefaultLimit(HttpHandler delegate) {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(
                DEFAULT_MAX_REQUESTS_PER_MINUTE, ONE_MINUTE, Clock.systemUTC());
        return new RateLimitingHandler(limiter, delegate);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!rateLimiter.tryAcquire(clientIdOf(exchange))) {
            rejectTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    // ubj: remote IP is correct only when clients connect directly; behind a
    // trusted proxy, switch to the proxy-supplied client address header
    private static String clientIdOf(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "Too many requests, please retry later.".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Retry-After", String.valueOf(rateLimiter.windowSeconds()));
        exchange.sendResponseHeaders(STATUS_TOO_MANY_REQUESTS, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
```

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SlidingWindowRateLimiterTest {

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT_A = "10.0.0.1";
    private static final String CLIENT_B = "10.0.0.2";

    /** Test clock that only moves when the test advances it. */
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-08-28T12:00:00Z");

        void advance(Duration amount) { now = now.plus(amount); }

        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }

    private final MutableClock clock = new MutableClock();
    private final SlidingWindowRateLimiter limiter =
            new SlidingWindowRateLimiter(MAX_REQUESTS, WINDOW, clock);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int request = 0; request < MAX_REQUESTS; request++) {
            assertTrue(limiter.tryAcquire(CLIENT_A));
        }
    }

    @Test
    void deniesTheRequestOverTheLimit() {
        for (int request = 0; request < MAX_REQUESTS; request++) {
            limiter.tryAcquire(CLIENT_A);
        }
        assertFalse(limiter.tryAcquire(CLIENT_A));
    }

    @Test
    void allowsAgainOnceTheWindowHasPassed() {
        for (int request = 0; request < MAX_REQUESTS; request++) {
            limiter.tryAcquire(CLIENT_A);
        }
        clock.advance(WINDOW.plusSeconds(1));
        assertTrue(limiter.tryAcquire(CLIENT_A));
    }

    @Test
    void limitsClientsIndependently() {
        for (int request = 0; request < MAX_REQUESTS; request++) {
            limiter.tryAcquire(CLIENT_A);
        }
        assertFalse(limiter.tryAcquire(CLIENT_A));
        assertTrue(limiter.tryAcquire(CLIENT_B));
    }

    @Test
    void slidesRatherThanResettingInFixedBlocks() {
        limiter.tryAcquire(CLIENT_A);
        clock.advance(Duration.ofSeconds(30));
        limiter.tryAcquire(CLIENT_A);
        limiter.tryAcquire(CLIENT_A);
        clock.advance(Duration.ofSeconds(31)); // first request has expired, later two have not
        assertTrue(limiter.tryAcquire(CLIENT_A));
        assertFalse(limiter.tryAcquire(CLIENT_A));
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> new SlidingWindowRateLimiter(0, WINDOW, clock));
        assertThrows(IllegalArgumentException.class,
                () -> new SlidingWindowRateLimiter(MAX_REQUESTS, Duration.ZERO, clock));
    }

    @Test
    void reportsWindowLengthInSeconds() {
        assertEquals(WINDOW.toSeconds(), limiter.windowSeconds());
    }
}
```

```java
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimitingHandlerTest {

    private static final int MAX_REQUESTS = 2;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final int STATUS_OK = 200;
    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private static final int NO_RESPONSE_BODY = -1;

    private HttpServer server;
    private URI endpoint;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void startServer() throws IOException {
        SlidingWindowRateLimiter limiter =
                new SlidingWindowRateLimiter(MAX_REQUESTS, WINDOW, Clock.systemUTC());
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", new RateLimitingHandler(limiter,
                exchange -> exchange.sendResponseHeaders(STATUS_OK, NO_RESPONSE_BODY)));
        server.start();
        endpoint = URI.create("http://localhost:" + server.getAddress().getPort() + "/");
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void passesRequestsThroughUntilTheLimitThenReturns429WithRetryAfter() throws Exception {
        for (int request = 0; request < MAX_REQUESTS; request++) {
            assertEquals(STATUS_OK, send().statusCode());
        }
        HttpResponse<Void> rejected = send();
        assertEquals(STATUS_TOO_MANY_REQUESTS, rejected.statusCode());
        assertEquals(String.valueOf(WINDOW.toSeconds()),
                rejected.headers().firstValue("Retry-After").orElse(""));
    }

    private HttpResponse<Void> send() throws Exception {
        return client.send(HttpRequest.newBuilder(endpoint).GET().build(),
                HttpResponse.BodyHandlers.discarding());
    }
}
```

Cleaned: the limiting logic lives in one clock-injected `SlidingWindowRateLimiter`, kept separate from the HTTP concern in `RateLimitingHandler`. Safe to change because the window algorithm, the client key, and the 429 response each sit in one small function, and the tests pin the limit boundary, window expiry, and per-client isolation.