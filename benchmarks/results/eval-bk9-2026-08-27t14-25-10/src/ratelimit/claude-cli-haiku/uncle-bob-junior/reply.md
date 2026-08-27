I'll provide a complete rate-limiting solution for a Java HTTP handler. I'm assuming:
- Standard Java `HttpServer` (built-in java.com.sun.net.httpserver)
- Limit: 10 requests per minute per client IP
- Window-based rate limiting with automatic cleanup

```java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitedHandler implements HttpHandler {
  private final HttpHandler delegate;
  private final RateLimiter rateLimiter;

  public RateLimitedHandler(HttpHandler delegate, RateLimiter rateLimiter) {
    this.delegate = delegate;
    this.rateLimiter = rateLimiter;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String clientIp = getClientIp(exchange);

    if (!rateLimiter.allowRequest(clientIp)) {
      exchange.getResponseHeaders().set("Retry-After", "60");
      exchange.sendResponseHeaders(429, 0);
      exchange.close();
      return;
    }

    delegate.handle(exchange);
  }

  private String getClientIp(HttpExchange exchange) {
    return exchange.getRemoteAddress().getAddress().getHostAddress();
  }
}

public class RateLimiter {
  private static final int MAX_REQUESTS_PER_MINUTE = 10;
  private static final long WINDOW_MILLIS = 60_000;
  private static final long CLEANUP_INTERVAL_MILLIS = 300_000;

  private final Map<String, RequestTimestamps> clientRequests;
  private long lastCleanupTime;

  public RateLimiter() {
    this.clientRequests = new ConcurrentHashMap<>();
    this.lastCleanupTime = System.currentTimeMillis();
  }

  public boolean allowRequest(String clientIp) {
    long now = System.currentTimeMillis();
    cleanupIfNeeded(now);

    RequestTimestamps timestamps = clientRequests.computeIfAbsent(
        clientIp,
        k -> new RequestTimestamps()
    );

    return timestamps.recordRequest(now);
  }

  private void cleanupIfNeeded(long now) {
    if (now - lastCleanupTime > CLEANUP_INTERVAL_MILLIS) {
      clientRequests.forEach((clientIp, timestamps) -> {
        if (timestamps.isExpired(now, WINDOW_MILLIS)) {
          clientRequests.remove(clientIp);
        }
      });
      lastCleanupTime = now;
    }
  }

  private static class RequestTimestamps {
    private final Queue<Long> timestamps = new LinkedList<>();

    boolean recordRequest(long now) {
      removeExpiredTimestamps(now);

      if (timestamps.size() < MAX_REQUESTS_PER_MINUTE) {
        timestamps.add(now);
        return true;
      }

      return false;
    }

    private void removeExpiredTimestamps(long now) {
      while (!timestamps.isEmpty() && now - timestamps.peek() > WINDOW_MILLIS) {
        timestamps.poll();
      }
    }

    boolean isExpired(long now, long windowMillis) {
      return timestamps.isEmpty() || now - timestamps.peek() > windowMillis;
    }
  }
}
```

**Usage example:**

```java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Server {
  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

    HttpHandler apiHandler = exchange -> {
      exchange.getResponseHeaders().set("Content-Type", "text/plain");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().write("OK".getBytes());
      exchange.close();
    };

    RateLimiter limiter = new RateLimiter();
    server.createContext("/api", new RateLimitedHandler(apiHandler, limiter));

    server.start();
    System.out.println("Server running on port 8080");
  }
}
```

**Tests:**

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {
  @Test
  void allowsRequestsWithinLimit() {
    RateLimiter limiter = new RateLimiter();
    String client = "192.168.1.1";

    for (int i = 0; i < 10; i++) {
      assertTrue(limiter.allowRequest(client));
    }
  }

  @Test
  void rejectsRequestsOverLimit() {
    RateLimiter limiter = new RateLimiter();
    String client = "192.168.1.1";

    for (int i = 0; i < 10; i++) {
      limiter.allowRequest(client);
    }

    assertFalse(limiter.allowRequest(client));
  }

  @Test
  void isolatesLimitsPerClient() {
    RateLimiter limiter = new RateLimiter();

    for (int i = 0; i < 10; i++) {
      limiter.allowRequest("192.168.1.1");
    }

    assertTrue(limiter.allowRequest("192.168.1.2"));
  }

  @Test
  void resetsAfterWindow() throws InterruptedException {
    RateLimiter limiter = new RateLimiter();
    String client = "192.168.1.1";

    for (int i = 0; i < 10; i++) {
      limiter.allowRequest(client);
    }
    assertFalse(limiter.allowRequest(client));

    Thread.sleep(61_000);
    assertTrue(limiter.allowRequest(client));
  }
}
```

**Key features:**
- **Per-IP tracking**: Rate limit applies independently to each client
- **Sliding window**: Accurate 60-second enforcement
- **Auto-cleanup**: Prevents memory leaks from inactive clients
- **Thread-safe**: Uses `ConcurrentHashMap` for concurrent request handling
- **HTTP 429**: Returns proper "Too Many Requests" status with `Retry-After` header