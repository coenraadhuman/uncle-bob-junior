Assumptions I'm making since none were specified:
- Target is the JDK's built-in `com.sun.net.httpserver.HttpHandler`/`HttpExchange` (no external server framework).
- Clients are identified by remote IP address.
- "A few requests per minute" → default of 5 requests per 60-second sliding window, exposed as a config value so it's easy to change.
- Algorithm: sliding window log (accurate, not the bursty edge-case behaviour of fixed windows).
- Tests use JUnit 5; the `HttpExchange` boundary (an abstract class from the JDK I don't own) is mocked with Mockito — everything else is tested against real objects.

```java
// RateLimitConfig.java
package com.example.ratelimit;

import java.time.Duration;

public record RateLimitConfig(int maxRequests, Duration window) {

    public RateLimitConfig {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("maxRequests must be positive");
        }
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window must be positive");
        }
    }
}
```

```java
// RateLimiter.java
package com.example.ratelimit;

import java.time.Instant;

/**
 * Decides whether a client may proceed with a request right now.
 * Implementations must be safe for concurrent use by multiple client IDs.
 */
public interface RateLimiter {

    /**
     * @param clientId identifier of the caller (e.g. IP address)
     * @param now      current instant, supplied by the caller for testability
     * @return true if the request is allowed and has been recorded, false if it must be rejected
     */
    boolean tryAcquire(String clientId, Instant now);
}
```

```java
// SlidingWindowRateLimiter.java
package com.example.ratelimit;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SlidingWindowRateLimiter implements RateLimiter {

    private final RateLimitConfig config;
    private final Map<String, Deque<Instant>> requestTimestampsByClient = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(RateLimitConfig config) {
        this.config = config;
    }

    @Override
    public boolean tryAcquire(String clientId, Instant now) {
        Deque<Instant> timestamps = requestTimestampsByClient.computeIfAbsent(clientId, id -> new ArrayDeque<>());
        synchronized (timestamps) {
            evictExpired(timestamps, now);
            if (timestamps.size() >= config.maxRequests()) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    private void evictExpired(Deque<Instant> timestamps, Instant now) {
        Instant windowStart = now.minus(config.window());
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
            timestamps.pollFirst();
        }
    }
}
```

```java
// RateLimitingHandler.java
package com.example.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

public final class RateLimitingHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    private final Clock clock;
    private final long retryAfterSeconds;

    public RateLimitingHandler(HttpHandler delegate, RateLimiter rateLimiter, Clock clock, Duration window) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        this.clock = clock;
        this.retryAfterSeconds = window.toSeconds();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdFrom(exchange);
        if (!rateLimiter.tryAcquire(clientId, clock.instant())) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdFrom(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set(RETRY_AFTER_HEADER, String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, -1);
        exchange.close();
    }
}
```

```java
// RateLimitedServerExample.java
package com.example.ratelimit;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;

public final class RateLimitedServerExample {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public static void main(String[] args) throws IOException {
        RateLimitConfig config = new RateLimitConfig(MAX_REQUESTS_PER_WINDOW, WINDOW);
        RateLimiter rateLimiter = new SlidingWindowRateLimiter(config);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RateLimitingHandler(
                exchange -> exchange.sendResponseHeaders(200, -1),
                rateLimiter,
                Clock.systemUTC(),
                WINDOW));
        server.start();
    }
}
```

```java
// SlidingWindowRateLimiterTest.java
package com.example.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowRateLimiterTest {

    private static final RateLimitConfig CONFIG = new RateLimitConfig(3, Duration.ofMinutes(1));
    private final SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(CONFIG);
    private final Instant start = Instant.parse("2026-08-28T10:00:00Z");

    @Test
    void allowsRequestsUpToLimit() {
        assertTrue(rateLimiter.tryAcquire("client-1", start));
        assertTrue(rateLimiter.tryAcquire("client-1", start.plusSeconds(1)));
        assertTrue(rateLimiter.tryAcquire("client-1", start.plusSeconds(2)));
    }

    @Test
    void deniesRequestBeyondLimitWithinWindow() {
        rateLimiter.tryAcquire("client-1", start);
        rateLimiter.tryAcquire("client-1", start.plusSeconds(1));
        rateLimiter.tryAcquire("client-1", start.plusSeconds(2));

        assertFalse(rateLimiter.tryAcquire("client-1", start.plusSeconds(3)));
    }

    @Test
    void allowsRequestAgainAfterWindowElapses() {
        rateLimiter.tryAcquire("client-1", start);
        rateLimiter.tryAcquire("client-1", start.plusSeconds(1));
        rateLimiter.tryAcquire("client-1", start.plusSeconds(2));

        assertTrue(rateLimiter.tryAcquire("client-1", start.plus(Duration.ofMinutes(1)).plusSeconds(1)));
    }

    @Test
    void tracksClientsIndependently() {
        rateLimiter.tryAcquire("client-1", start);
        rateLimiter.tryAcquire("client-1", start.plusSeconds(1));
        rateLimiter.tryAcquire("client-1", start.plusSeconds(2));

        assertTrue(rateLimiter.tryAcquire("client-2", start.plusSeconds(2)));
    }
}
```

```java
// RateLimitingHandlerTest.java
package com.example.ratelimit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RateLimitingHandlerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-08-28T10:00:00Z"), ZoneOffset.UTC);
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT_IP = "127.0.0.1";

    @Test
    void forwardsRequestWhenUnderLimit() throws IOException {
        HttpExchange exchange = exchangeFrom(CLIENT_IP);
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimiter rateLimiter = mock(RateLimiter.class);
        when(rateLimiter.tryAcquire(eq(CLIENT_IP), any())).thenReturn(true);

        new RateLimitingHandler(delegate, rateLimiter, FIXED_CLOCK, WINDOW).handle(exchange);

        verify(delegate).handle(exchange);
        verify(exchange, never()).sendResponseHeaders(eq(429), anyLong());
    }

    @Test
    void rejectsRequestWhenOverLimit() throws IOException {
        HttpExchange exchange = exchangeFrom(CLIENT_IP);
        HttpHandler delegate = mock(HttpHandler.class);
        RateLimiter rateLimiter = mock(RateLimiter.class);
        when(rateLimiter.tryAcquire(eq(CLIENT_IP), any())).thenReturn(false);

        new RateLimitingHandler(delegate, rateLimiter, FIXED_CLOCK, WINDOW).handle(exchange);

        verify(delegate, never()).handle(any());
        verify(exchange).sendResponseHeaders(429, -1);
    }

    private HttpExchange exchangeFrom(String clientIp) {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress(clientIp, 1234));
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        return exchange;
    }
}
```

Cleaned: rate-limiting is one seam (`RateLimiter`) behind the HTTP concern, so the sliding-window algorithm is testable with plain `Instant` values and no server. `RateLimitingHandler` does one job (reject or delegate), and swapping the algorithm or the client-identification strategy is a one-place change.