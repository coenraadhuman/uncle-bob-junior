I'll create a rate limiter implementation for Java HTTP handlers with clear separation of concerns and configurable limits.

```java
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final int DEFAULT_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_SIZE_MILLIS = 60_000;

    private final int requestsPerMinute;
    private final Map<String, Deque<Long>> clientRequests;

    public RateLimiter(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
        this.clientRequests = new ConcurrentHashMap<>();
    }

    public RateLimiter() {
        this(DEFAULT_REQUESTS_PER_MINUTE);
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequests.computeIfAbsent(clientId, _ -> new ConcurrentLinkedDeque<>());

        removeExpiredRequests(timestamps, now);

        if (timestamps.size() >= requestsPerMinute) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    public int remainingRequests(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequests.get(clientId);

        if (timestamps == null) {
            return requestsPerMinute;
        }

        removeExpiredRequests(timestamps, now);
        return Math.max(0, requestsPerMinute - timestamps.size());
    }

    private void removeExpiredRequests(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && timestamps.peekFirst() <= now - WINDOW_SIZE_MILLIS) {
            timestamps.removeFirst();
        }
    }

    public void reset(String clientId) {
        clientRequests.remove(clientId);
    }
}
```

```java
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RateLimitedHttpHandler implements HttpHandler {
    private final RateLimiter rateLimiter;
    private final HttpHandler delegate;

    public RateLimitedHttpHandler(HttpHandler delegate, RateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = extractClientId(exchange);

        if (!rateLimiter.allowRequest(clientId)) {
            sendRateLimitExceeded(exchange, clientId);
            return;
        }

        addRateLimitHeaders(exchange, clientId);
        delegate.handle(exchange);
    }

    private void sendRateLimitExceeded(HttpExchange exchange, String clientId) throws IOException {
        addRateLimitHeaders(exchange, clientId);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Retry-After", "60");

        String response = "{\"error\":\"Rate limit exceeded. Max 10 requests per minute.\"}";
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(429, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    private void addRateLimitHeaders(HttpExchange exchange, String clientId) {
        int remaining = rateLimiter.remainingRequests(clientId);
        exchange.getResponseHeaders().set("X-RateLimit-Remaining", String.valueOf(remaining));
        exchange.getResponseHeaders().set("X-RateLimit-Limit", "10");
        exchange.getResponseHeaders().set("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60_000));
    }

    private String extractClientId(HttpExchange exchange) {
        String xForwarded = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isEmpty()) {
            return xForwarded.split(",")[0].trim();
        }
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}
```

```java
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ExampleApiHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "{\"status\":\"ok\",\"message\":\"Request processed\"}";
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }
}
```

```java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class RateLimitedServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        RateLimiter rateLimiter = new RateLimiter(10);
        HttpHandler apiHandler = new ExampleApiHandler();
        HttpHandler rateLimitedHandler = new RateLimitedHttpHandler(apiHandler, rateLimiter);

        server.createContext("/api", rateLimitedHandler);
        server.start();
        System.out.println("Server started on http://localhost:8080");
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {
    private static final String CLIENT_ID = "192.168.1.1";

    @Test
    public void allowsRequestsUnderLimit() {
        RateLimiter limiter = new RateLimiter(5);

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allowRequest(CLIENT_ID));
        }
    }

    @Test
    public void deniesRequestsAboveLimit() {
        RateLimiter limiter = new RateLimiter(3);

        for (int i = 0; i < 3; i++) {
            limiter.allowRequest(CLIENT_ID);
        }

        assertFalse(limiter.allowRequest(CLIENT_ID));
    }

    @Test
    public void tracksRemainingRequests() {
        RateLimiter limiter = new RateLimiter(5);

        limiter.allowRequest(CLIENT_ID);
        limiter.allowRequest(CLIENT_ID);

        assertEquals(3, limiter.remainingRequests(CLIENT_ID));
    }

    @Test
    public void resetsClientAfterReset() {
        RateLimiter limiter = new RateLimiter(2);

        limiter.allowRequest(CLIENT_ID);
        limiter.allowRequest(CLIENT_ID);
        limiter.reset(CLIENT_ID);

        assertEquals(2, limiter.remainingRequests(CLIENT_ID));
    }

    @Test
    public void distinguishesDifferentClients() {
        RateLimiter limiter = new RateLimiter(2);

        limiter.allowRequest("client1");
        limiter.allowRequest("client1");
        assertTrue(limiter.allowRequest("client2"));
    }
}
```

**Key design decisions:**

- **Sliding window**: Tracks individual request timestamps so limits reset naturally after 60 seconds rather than on calendar boundaries
- **Per-IP tracking**: Extracts client IP, checking `X-Forwarded-For` for proxied requests
- **Thread-safe**: Uses `ConcurrentHashMap` and `ConcurrentLinkedDeque` for multi-threaded environments
- **Memory cleanup**: Old requests expire automatically when checked; aged entries are removed
- **Standard headers**: Returns `X-RateLimit-*` headers and HTTP 429 with `Retry-After` following web standards
- **Configurable limit**: Defaults to 10/minute, accepts custom thresholds in the constructor

Wrap any handler with `new RateLimitedHttpHandler(handler, rateLimiter)` to apply rate limiting.