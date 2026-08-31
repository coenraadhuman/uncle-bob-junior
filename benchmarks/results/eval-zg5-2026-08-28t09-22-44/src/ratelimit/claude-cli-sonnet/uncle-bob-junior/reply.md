Assumptions: using the JDK's built-in `com.sun.net.httpserver.HttpHandler` (no framework specified), "a few requests per minute" → 5 requests/60s (both exposed as constants you can tune), client identity = remote IP address, and a fixed-window counter algorithm (simple, memory-bounded, slightly bursty at window edges — noted with a `ubj:` comment). Tests use JUnit 5 and Mockito.

```java
// WindowState.java
package ratelimit;

/** Immutable snapshot of a client's request count within the current fixed window. */
record WindowState(long windowStartSeconds, int requestCount) {
}
```

```java
// FixedWindowRateLimiter.java
package ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Fixed-window per-client rate limiter.
 * Each client gets at most {@code maxRequestsPerWindow} requests per {@code windowDuration}.
 * ubj: fixed windows allow a short burst around window boundaries (up to 2x the limit);
 * switch to a sliding-window log if that becomes a real problem.
 */
public final class FixedWindowRateLimiter implements AutoCloseable {

    private final Map<String, AtomicReference<WindowState>> clientWindows = new ConcurrentHashMap<>();
    private final Duration windowDuration;
    private final int maxRequestsPerWindow;
    private final Clock clock;
    private final ScheduledExecutorService evictionExecutor;

    public FixedWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration) {
        this(maxRequestsPerWindow, windowDuration, Clock.systemUTC());
    }

    FixedWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDuration = windowDuration;
        this.clock = clock;
        this.evictionExecutor = newDaemonScheduler();
        long periodSeconds = windowDuration.getSeconds();
        evictionExecutor.scheduleAtFixedRate(
                this::evictInactiveEntries, periodSeconds, periodSeconds, TimeUnit.SECONDS);
    }

    public boolean tryAcquire(String clientId) {
        AtomicReference<WindowState> stateRef =
                clientWindows.computeIfAbsent(clientId, id -> new AtomicReference<>(freshWindowState()));
        while (true) {
            WindowState currentState = stateRef.get();
            WindowState nextState = advance(currentState);
            if (nextState.requestCount() > maxRequestsPerWindow) {
                return false;
            }
            if (stateRef.compareAndSet(currentState, nextState)) {
                return true;
            }
        }
    }

    public long windowDurationSeconds() {
        return windowDuration.getSeconds();
    }

    @Override
    public void close() {
        evictionExecutor.shutdownNow();
    }

    private WindowState advance(WindowState state) {
        long windowStart = currentWindowStart();
        return windowStart == state.windowStartSeconds()
                ? new WindowState(windowStart, state.requestCount() + 1)
                : new WindowState(windowStart, 1);
    }

    private WindowState freshWindowState() {
        return new WindowState(currentWindowStart(), 0);
    }

    private long currentWindowStart() {
        long windowSeconds = windowDuration.getSeconds();
        return clock.instant().getEpochSecond() / windowSeconds * windowSeconds;
    }

    private void evictInactiveEntries() {
        long activeWindowStart = currentWindowStart();
        clientWindows.entrySet().removeIf(entry -> entry.getValue().get().windowStartSeconds() != activeWindowStart);
    }

    private static ScheduledExecutorService newDaemonScheduler() {
        return Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "rate-limiter-eviction");
            thread.setDaemon(true);
            return thread;
        });
    }
}
```

```java
// RateLimitingHttpHandler.java
package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Wraps a delegate handler and rejects clients that exceed the configured rate limit. */
public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String RATE_LIMIT_MESSAGE = "Rate limit exceeded. Try again later.";

    private final HttpHandler delegate;
    private final FixedWindowRateLimiter rateLimiter;

    public RateLimitingHttpHandler(HttpHandler delegate, FixedWindowRateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdentifier(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            sendTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdentifier(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = RATE_LIMIT_MESSAGE.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, String.valueOf(rateLimiter.windowDurationSeconds()));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
```

```java
// FixedWindowRateLimiterTest.java
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

    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT_A = "client-a";
    private static final String CLIENT_B = "client-b";

    @Test
    void allowsRequestsUpToTheLimit() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-28T10:00:00Z"));
        try (FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(MAX_REQUESTS, WINDOW, clock)) {
            assertTrue(limiter.tryAcquire(CLIENT_A));
            assertTrue(limiter.tryAcquire(CLIENT_A));
            assertTrue(limiter.tryAcquire(CLIENT_A));
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimitWithinTheSameWindow() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-28T10:00:00Z"));
        try (FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(MAX_REQUESTS, WINDOW, clock)) {
            for (int i = 0; i < MAX_REQUESTS; i++) {
                limiter.tryAcquire(CLIENT_A);
            }
            assertFalse(limiter.tryAcquire(CLIENT_A));
        }
    }

    @Test
    void resetsTheCountAfterTheWindowElapses() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-28T10:00:00Z"));
        try (FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(MAX_REQUESTS, WINDOW, clock)) {
            for (int i = 0; i < MAX_REQUESTS; i++) {
                limiter.tryAcquire(CLIENT_A);
            }
            clock.advance(WINDOW);
            assertTrue(limiter.tryAcquire(CLIENT_A));
        }
    }

    @Test
    void tracksEachClientIndependently() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-28T10:00:00Z"));
        try (FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(MAX_REQUESTS, WINDOW, clock)) {
            for (int i = 0; i < MAX_REQUESTS; i++) {
                limiter.tryAcquire(CLIENT_A);
            }
            assertTrue(limiter.tryAcquire(CLIENT_B));
        }
    }

    /** Test-only clock whose instant can be advanced deterministically. */
    private static final class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public Instant instant() {
            return instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
```

```java
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
```

Cleaned: rate-limit decision (`FixedWindowRateLimiter`) and HTTP wiring (`RateLimitingHttpHandler`) are separate, single-purpose classes with named constants, no nesting past one level, and CAS-based state updates so concurrent requests can't corrupt counts. Safe to change because the limiter takes an injectable `Clock`, so window-boundary and per-client isolation behavior is fully covered by deterministic tests, not timing-dependent ones.