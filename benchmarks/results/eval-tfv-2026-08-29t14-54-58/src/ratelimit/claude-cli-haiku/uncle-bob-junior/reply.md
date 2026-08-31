I'll create a rate limiter that tracks requests per client IP and enforces a limit of 5 requests per 60 seconds. The solution includes the core limiter, a servlet filter to apply it, and tests.

**Assumptions:** client identified by IP address, 5 requests/minute limit, in-memory storage with automatic cleanup of old entries.

```java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
  private static final int MAX_REQUESTS = 5;
  private static final long WINDOW_MILLIS = 60_000;
  private static final long CLEANUP_INTERVAL_MILLIS = 300_000;

  private final Map<String, Deque<Long>> requestsByClient = new ConcurrentHashMap<>();
  private volatile long lastCleanup = System.currentTimeMillis();

  public boolean allowRequest(String clientId) {
    cleanupIfStale();
    long now = System.currentTimeMillis();
    Deque<Long> timestamps = requestsByClient.computeIfAbsent(clientId, _ -> new ConcurrentLinkedDeque<>());

    removeExpiredTimestamps(timestamps, now);
    if (timestamps.size() >= MAX_REQUESTS) {
      return false;
    }

    timestamps.add(now);
    return true;
  }

  private void removeExpiredTimestamps(Deque<Long> timestamps, long now) {
    while (!timestamps.isEmpty() && now - timestamps.getFirst() >= WINDOW_MILLIS) {
      timestamps.removeFirst();
    }
  }

  private void cleanupIfStale() {
    long now = System.currentTimeMillis();
    if (now - lastCleanup < CLEANUP_INTERVAL_MILLIS) {
      return;
    }

    lastCleanup = now;
    requestsByClient.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }
}
```

```java
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

public class RateLimitingFilter implements Filter {
  private final RateLimiter limiter = new RateLimiter();

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String clientIp = extractClientIp(request);

    if (!limiter.allowRequest(clientIp)) {
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      httpResponse.sendError(429, "Too Many Requests");
      return;
    }

    chain.doFilter(request, response);
  }

  private String extractClientIp(ServletRequest request) {
    String forwardedFor = request.getServletContext()
        .getAttribute("X-Forwarded-For") != null
        ? (String) request.getServletContext().getAttribute("X-Forwarded-For")
        : null;
    return forwardedFor != null ? forwardedFor.split(",")[0].trim() : request.getRemoteAddr();
  }

  @Override
  public void init(FilterConfig config) {}

  @Override
  public void destroy() {}
}
```

```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class RateLimiterTest {
  private final RateLimiter limiter = new RateLimiter();

  @Test
  void allowsRequestsWithinLimit() {
    String clientId = "192.168.1.1";
    for (int i = 0; i < 5; i++) {
      assertTrue(limiter.allowRequest(clientId));
    }
  }

  @Test
  void rejectsRequestsExceedingLimit() {
    String clientId = "192.168.1.1";
    for (int i = 0; i < 5; i++) {
      limiter.allowRequest(clientId);
    }
    assertFalse(limiter.allowRequest(clientId));
  }

  @Test
  void allowsDifferentClientsIndependently() {
    assertTrue(limiter.allowRequest("client1"));
    assertTrue(limiter.allowRequest("client2"));
    for (int i = 0; i < 4; i++) {
      limiter.allowRequest("client1");
    }
    assertFalse(limiter.allowRequest("client1"));
    assertTrue(limiter.allowRequest("client2"));
  }

  @Test
  void resetsAfterWindowExpires() throws InterruptedException {
    String clientId = "192.168.1.1";
    for (int i = 0; i < 5; i++) {
      limiter.allowRequest(clientId);
    }
    assertFalse(limiter.allowRequest(clientId));

    Thread.sleep(61_000);
    assertTrue(limiter.allowRequest(clientId));
  }
}
```

**Integration:** Register `RateLimitingFilter` in your `web.xml` or via `@WebFilter` annotation to protect all endpoints. The limiter uses a sliding-window approach with client IP tracking and automatic cleanup of stale entries.

Cleaned: rate limiting isolated into single-responsibility classes; intent-revealing names (`removeExpiredTimestamps`, `cleanupIfStale`); guard clause for rejected requests; named constants for limits and timeouts; tests cover happy path (within limit), boundary (exactly at limit), independence (different clients), and window expiry.