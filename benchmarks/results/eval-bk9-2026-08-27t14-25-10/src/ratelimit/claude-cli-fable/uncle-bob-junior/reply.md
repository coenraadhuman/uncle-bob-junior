Assumptions: JDK built-in `com.sun.net.httpserver` handler, clients identified by remote IP (behind a proxy you would key on a trusted `X-Forwarded-For` instead), limit of 5 requests per rolling minute, Java 17+, JUnit 5. Over-limit requests get `429 Too Many Requests` with a `Retry-After` header.

```java
package ratelimit;

import java.time.Duration;

/** Outcome of a rate-limit check; {@code retryAfter} is zero when the request is allowed. */
public record RateLimitDecision(boolean allowed, Duration retryAfter) {

    public static RateLimitDecision allowed() {
        return new RateLimitDecision(true, Duration.ZERO);
    }

    public static RateLimitDecision rejectedFor(Duration retryAfter) {
        return new RateLimitDecision(false, retryAfter);
    }
}
```

```java
package ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sliding-window rate limiter: each client may make at most
 * {@code maxRequestsPerWindow} requests within any rolling {@code window}.
 * Thread-safe: per-client state is only touched inside the map's atomic compute.
 */
public final class SlidingWindowRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Instant>> requestTimesByClient =
            new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration window, Clock clock) {
        if (maxRequestsPerWindow < 1) {
            throw new IllegalArgumentException("maxRequestsPerWindow must be at least 1");
        }
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window must be positive");
        }
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** Records the request if the client is under its limit and reports the decision. */
    public RateLimitDecision check(String clientId) {
        AtomicReference<RateLimitDecision> decision = new AtomicReference<>();
        requestTimesByClient.compute(clientId, (id, existingTimes) -> {
            Deque<Instant> times = existingTimes == null ? new ArrayDeque<>() : existingTimes;
            decision.set(decide(times));
            return times;
        });
        return decision.get();
    }

    /** Drops state for clients whose requests have all left the window. Call periodically. */
    public void purgeIdleClients() {
        Instant cutoff = clock.instant().minus(window);
        for (String clientId : requestTimesByClient.keySet()) {
            requestTimesByClient.computeIfPresent(clientId, (id, times) -> {
                evictOlderThan(times, cutoff);
                return times.isEmpty() ? null : times;
            });
        }
    }

    private RateLimitDecision decide(Deque<Instant> times) {
        Instant now = clock.instant();
        evictOlderThan(times, now.minus(window));
        if (times.size() >= maxRequestsPerWindow) {
            return RateLimitDecision.rejectedFor(timeUntilOldestExpires(times, now));
        }
        times.addLast(now);
        return RateLimitDecision.allowed();
    }

    private Duration timeUntilOldestExpires(Deque<Instant> times, Instant now) {
        return Duration.between(now, times.peekFirst().plus(window));
    }

    private static void evictOlderThan(Deque<Instant> times, Instant cutoff) {
        while (!times.isEmpty() && !times.peekFirst().isAfter(cutoff)) {
            times.removeFirst();
        }
    }

    int trackedClientCount() {
        // ubj: package-private, exists only so tests can verify purge frees memory
        return requestTimesByClient.size();
    }
}
```

```java
package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Wraps another handler and rejects clients that exceed the rate limit with
 * 429 Too Many Requests and a Retry-After header (in seconds).
 * Clients are identified by remote IP address, so deploy behind a proxy only
 * if the proxy preserves the real client address.
 */
public final class RateLimitingHandler implements HttpHandler {

    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final long MILLIS_PER_SECOND = 1_000;
    private static final byte[] REJECTION_BODY =
            "Too many requests, please retry later.".getBytes(StandardCharsets.UTF_8);

    private final SlidingWindowRateLimiter rateLimiter;
    private final HttpHandler delegate;

    public RateLimitingHandler(SlidingWindowRateLimiter rateLimiter, HttpHandler delegate) {
        this.rateLimiter = rateLimiter;
        this.delegate = delegate;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        RateLimitDecision decision = rateLimiter.check(clientIdOf(exchange));
        if (!decision.allowed()) {
            reject(exchange, decision);
            return;
        }
        delegate.handle(exchange);
    }

    private static String clientIdOf(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private static void reject(HttpExchange exchange, RateLimitDecision decision) throws IOException {
        exchange.getResponseHeaders().set(RETRY_AFTER_HEADER, Long.toString(retryAfterSeconds(decision)));
        exchange.sendResponseHeaders(STATUS_TOO_MANY_REQUESTS, REJECTION_BODY.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(REJECTION_BODY);
        }
    }

    private static long retryAfterSeconds(RateLimitDecision decision) {
        // Round up so clients never retry a moment before a slot actually frees.
        long millis = decision.retryAfter().toMillis();
        return Math.max(1, (millis + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND);
    }
}
```

```java
package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Example wiring: a small server whose /hello endpoint is rate limited per client IP. */
public final class RateLimitedServer {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);
    private static final int STATUS_OK = 200;

    private RateLimitedServer() {
    }

    public static void main(String[] args) throws IOException {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(
                MAX_REQUESTS_PER_MINUTE, RATE_LIMIT_WINDOW, Clock.systemUTC());
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/hello", new RateLimitingHandler(limiter, RateLimitedServer::sayHello));
        scheduleIdleClientPurge(limiter);
        server.start();
    }

    private static void scheduleIdleClientPurge(SlidingWindowRateLimiter limiter) {
        ScheduledExecutorService housekeeping = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "rate-limit-housekeeping");
            thread.setDaemon(true);
            return thread;
        });
        long periodSeconds = RATE_LIMIT_WINDOW.toSeconds();
        housekeeping.scheduleAtFixedRate(
                limiter::purgeIdleClients, periodSeconds, periodSeconds, TimeUnit.SECONDS);
    }

    private static void sayHello(HttpExchange exchange) throws IOException {
        byte[] body = "Hello!".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(STATUS_OK, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
```

Tests. First a controllable clock, then unit tests for the limiter core, then an integration test proving the handler's HTTP behavior end to end:

```java
package ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Test clock that only moves when told to. */
final class MutableClock extends Clock {

    private Instant now;

    MutableClock(Instant start) {
        this.now = start;
    }

    void advance(Duration amount) {
        now = now.plus(amount);
    }

    @Override
    public Instant instant() {
        return now;
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
```

```java
package ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowRateLimiterTest {

    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT = "client-a";
    private static final String OTHER_CLIENT = "client-b";

    private final MutableClock clock = new MutableClock(Instant.parse("2026-08-27T10:00:00Z"));
    private final SlidingWindowRateLimiter limiter =
            new SlidingWindowRateLimiter(LIMIT, WINDOW, clock);

    @Test
    void allowsRequestsUpToTheLimit() {
        for (int request = 0; request < LIMIT; request++) {
            assertTrue(limiter.check(CLIENT).allowed());
        }
    }

    @Test
    void rejectsTheRequestThatExceedsTheLimit() {
        exhaustLimit(CLIENT);
        assertFalse(limiter.check(CLIENT).allowed());
    }

    @Test
    void countsEachClientSeparately() {
        exhaustLimit(CLIENT);
        assertTrue(limiter.check(OTHER_CLIENT).allowed());
    }

    @Test
    void freesOnlyTheSlotsThatHaveLeftTheWindow() {
        limiter.check(CLIENT);
        clock.advance(Duration.ofSeconds(30));
        limiter.check(CLIENT);
        limiter.check(CLIENT);
        clock.advance(Duration.ofSeconds(30));

        assertTrue(limiter.check(CLIENT).allowed(), "first request expired, slot is free");
        assertFalse(limiter.check(CLIENT).allowed(), "the two mid-window requests still count");
    }

    @Test
    void reportsHowLongUntilASlotFreesUp() {
        exhaustLimit(CLIENT);
        clock.advance(Duration.ofSeconds(15));

        RateLimitDecision decision = limiter.check(CLIENT);

        assertFalse(decision.allowed());
        assertEquals(Duration.ofSeconds(45), decision.retryAfter());
    }

    @Test
    void allowedDecisionNeedsNoWait() {
        assertEquals(Duration.ZERO, limiter.check(CLIENT).retryAfter());
    }

    @Test
    void purgeDropsClientsWhoseRequestsHaveAllExpired() {
        limiter.check(CLIENT);
        clock.advance(WINDOW.plusSeconds(1));
        limiter.check(OTHER_CLIENT);

        limiter.purgeIdleClients();

        assertEquals(1, limiter.trackedClientCount());
    }

    @Test
    void purgeKeepsRequestsThatAreStillInsideTheWindow() {
        exhaustLimit(CLIENT);
        clock.advance(Duration.ofSeconds(30));

        limiter.purgeIdleClients();

        assertFalse(limiter.check(CLIENT).allowed());
    }

    @Test
    void rejectsALimitBelowOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new SlidingWindowRateLimiter(0, WINDOW, clock));
    }

    @Test
    void rejectsANonPositiveWindow() {
        assertThrows(IllegalArgumentException.class,
                () -> new SlidingWindowRateLimiter(LIMIT, Duration.ZERO, clock));
    }

    private void exhaustLimit(String clientId) {
        for (int request = 0; request < LIMIT; request++) {
            limiter.check(clientId);
        }
    }
}
```

```java
package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** All requests come from 127.0.0.1, so they share one client's limit. */
class RateLimitingHandlerTest {

    private static final int MAX_REQUESTS = 2;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final int STATUS_OK = 200;
    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private static final String DELEGATE_BODY = "hello";

    private HttpServer server;
    private HttpClient client;

    @BeforeEach
    void startServer() throws IOException {
        SlidingWindowRateLimiter limiter =
                new SlidingWindowRateLimiter(MAX_REQUESTS, WINDOW, Clock.systemUTC());
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", new RateLimitingHandler(limiter, RateLimitingHandlerTest::okDelegate));
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void passesRequestsUnderTheLimitToTheDelegate() throws Exception {
        for (int request = 0; request < MAX_REQUESTS; request++) {
            HttpResponse<String> response = get();
            assertEquals(STATUS_OK, response.statusCode());
            assertEquals(DELEGATE_BODY, response.body());
        }
    }

    @Test
    void rejectsRequestsOverTheLimitWith429AndRetryAfter() throws Exception {
        for (int request = 0; request < MAX_REQUESTS; request++) {
            get();
        }

        HttpResponse<String> rejected = get();

        assertEquals(STATUS_TOO_MANY_REQUESTS, rejected.statusCode());
        long retryAfterSeconds = Long.parseLong(
                rejected.headers().firstValue("Retry-After").orElseThrow());
        assertTrue(retryAfterSeconds >= 1 && retryAfterSeconds <= WINDOW.toSeconds());
    }

    private HttpResponse<String> get() throws IOException, InterruptedException {
        URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/");
        return client.send(HttpRequest.newBuilder(uri).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
```

Cleaned: limiting logic isolated in a pure-ish core (`SlidingWindowRateLimiter` with an injected `Clock`), HTTP concerns kept in a thin decorator, idle-client purge prevents unbounded memory growth. Safe to change because the limit, window, and client-identity strategy each live in exactly one place, and the tests pin both the sliding-window arithmetic and the wire-level 429 behavior.