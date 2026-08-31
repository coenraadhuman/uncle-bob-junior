Assumptions: identifying a client by remote IP address (add `X-Forwarded-For` handling separately if you're behind a proxy/load balancer); using a fixed-window counter (simplest correct algorithm for "N requests per minute"); state is in-memory and per-instance (swap the map for Redis/etc. if you run multiple server instances); built against `com.sun.net.httpserver.HttpHandler` since that's the JDK's built-in HTTP server — adapt `extractClientId` if you're on Servlet/Spring instead.

```java
package ratelimit;

/**
 * Immutable snapshot of a client's request count within the current fixed window.
 */
record WindowState(long windowStartMillis, int requestCount) {
}
```

```java
package ratelimit;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/**
 * Fixed-window rate limiter. Each client id gets its own window of
 * {@code windowDuration}; once {@code maxRequestsPerWindow} requests have
 * been recorded, further calls to {@link #tryAcquire(String)} return false
 * until the window rolls over.
 */
public final class RateLimiter {

    private static final int INITIAL_REQUEST_COUNT = 1;

    private final int maxRequestsPerWindow;
    private final long windowDurationMillis;
    private final LongSupplier currentTimeMillis;

    // ubj: unbounded map, one entry per distinct client id forever; add eviction
    // (e.g. Caffeine with expireAfterAccess) if this runs long enough to see
    // many unique clients.
    private final ConcurrentMap<String, AtomicReference<WindowState>> clientWindows =
            new ConcurrentHashMap<>();

    public RateLimiter(int maxRequestsPerWindow, Duration windowDuration) {
        this(maxRequestsPerWindow, windowDuration, System::currentTimeMillis);
    }

    RateLimiter(int maxRequestsPerWindow, Duration windowDuration, LongSupplier currentTimeMillis) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDurationMillis = windowDuration.toMillis();
        this.currentTimeMillis = currentTimeMillis;
    }

    /** Returns true if the client may proceed, false if it has exceeded its limit for the current window. */
    public boolean tryAcquire(String clientId) {
        AtomicReference<WindowState> stateRef =
                clientWindows.computeIfAbsent(clientId, id -> new AtomicReference<>());
        long now = currentTimeMillis.getAsLong();
        WindowState current;
        WindowState next;
        do {
            current = stateRef.get();
            next = nextState(current, now);
            if (next == null) {
                return false;
            }
        } while (!stateRef.compareAndSet(current, next));
        return true;
    }

    private WindowState nextState(WindowState current, long now) {
        if (current == null || isWindowExpired(current, now)) {
            return new WindowState(now, INITIAL_REQUEST_COUNT);
        }
        if (current.requestCount() >= maxRequestsPerWindow) {
            return null;
        }
        return new WindowState(current.windowStartMillis(), current.requestCount() + 1);
    }

    private boolean isWindowExpired(WindowState state, long now) {
        return now - state.windowStartMillis() >= windowDurationMillis;
    }
}
```

```java
package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.Duration;

/**
 * Wraps a delegate {@link HttpHandler}, rejecting requests from clients who
 * have exceeded {@code rateLimiter}'s configured limit with HTTP 429.
 */
public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int NO_RESPONSE_BODY = -1;
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    private final long retryAfterSeconds;

    public RateLimitingHttpHandler(HttpHandler delegate, RateLimiter rateLimiter, Duration windowDuration) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        this.retryAfterSeconds = windowDuration.toSeconds();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = extractClientId(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private static String extractClientId(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, NO_RESPONSE_BODY);
        exchange.close();
    }
}
```

```java
package ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    private static final int MAX_REQUESTS_PER_WINDOW = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    @Test
    void allowsUpToTheLimitWithinAWindow() {
        long[] now = {0L};
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW, () -> now[0]);

        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            assertTrue(limiter.tryAcquire("client-1"));
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimitWithinAWindow() {
        long[] now = {0L};
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW, () -> now[0]);

        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            limiter.tryAcquire("client-1");
        }

        assertFalse(limiter.tryAcquire("client-1"));
    }

    @Test
    void resetsTheCountAfterTheWindowElapses() {
        long[] now = {0L};
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW, () -> now[0]);
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            limiter.tryAcquire("client-1");
        }
        assertFalse(limiter.tryAcquire("client-1"));

        now[0] += WINDOW.toMillis();

        assertTrue(limiter.tryAcquire("client-1"));
    }

    @Test
    void tracksEachClientIndependently() {
        long[] now = {0L};
        RateLimiter limiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW, () -> now[0]);
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            limiter.tryAcquire("client-1");
        }

        assertTrue(limiter.tryAcquire("client-2"));
    }
}
```

```java
package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitingHttpHandlerTest {

    private static final int MAX_REQUESTS_PER_WINDOW = 3;
    private static final int HTTP_OK = 200;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int NO_RESPONSE_BODY = -1;

    @Test
    void allowsRequestsUpToLimitThenRejectsFurtherRequests() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, Duration.ofMinutes(1));
        HttpHandler okHandler = RateLimitingHttpHandlerTest::respondOk;
        server.createContext("/", new RateLimitingHttpHandler(okHandler, rateLimiter, Duration.ofMinutes(1)));
        server.start();

        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:" + server.getAddress().getPort() + "/");

            for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
                assertEquals(HTTP_OK, sendGet(client, uri));
            }
            assertEquals(HTTP_TOO_MANY_REQUESTS, sendGet(client, uri));
        } finally {
            server.stop(0);
        }
    }

    private static int sendGet(HttpClient client, URI uri) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        return response.statusCode();
    }

    private static void respondOk(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(HTTP_OK, NO_RESPONSE_BODY);
        exchange.close();
    }
}
```

`RateLimitingHttpHandler` decorates any `HttpHandler` → cleaned: single-responsibility split between counting (`RateLimiter`, lock-free CAS, fixed-window) and HTTP concerns (429 + `Retry-After`), safe to change because the two are tested independently and the wrapper never touches your existing handler logic.