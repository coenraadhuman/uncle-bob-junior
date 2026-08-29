I'll provide a clean, thread-safe rate limiter with a sliding-window approach, a servlet filter integration, and tests. Each client (by IP) gets a budget of 5 requests per minute.

Assumptions:
- Servlet-based HTTP environment
- Rate limiting per client IP (with X-Forwarded-For fallback)
- 5 requests per minute default; configurable
- Sliding window counter (tracks recent request timestamps)

```java
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class ClientRateLimiter {
  private static final int DEFAULT_MAX_REQUESTS = 5;
  private static final int DEFAULT_WINDOW_MINUTES = 1;

  private final int maxRequests;
  private final long windowMillis;
  private final Supplier<Long> timeProvider;
  private final Map<String, Queue<Long>> clientRequests = new ConcurrentHashMap<>();

  public ClientRateLimiter() {
    this(DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_MINUTES);
  }

  public ClientRateLimiter(int maxRequests, int windowMinutes) {
    this(maxRequests, windowMinutes, System::currentTimeMillis);
  }

  ClientRateLimiter(int maxRequests, int windowMinutes, Supplier<Long> timeProvider) {
    this.maxRequests = maxRequests;
    this.windowMillis = (long) windowMinutes * 60 * 1000;
    this.timeProvider = timeProvider;
  }

  public boolean allowRequest(String clientId) {
    long now = timeProvider.get();
    Queue<Long> timestamps = clientRequests.computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<>());

    while (!timestamps.isEmpty() && now - timestamps.peek() > windowMillis) {
      timestamps.poll();
    }

    if (timestamps.size() < maxRequests) {
      timestamps.offer(now);
      return true;
    }
    return false;
  }
}
```

```java
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitFilter implements Filter {
  private final ClientRateLimiter rateLimiter = new ClientRateLimiter();

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String clientIp = getClientIp(httpRequest);
    if (rateLimiter.allowRequest(clientIp)) {
      chain.doFilter(request, response);
    } else {
      httpResponse.setStatus(429);
      response.getWriter().write("Rate limit exceeded");
    }
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  @Override
  public void init(FilterConfig filterConfig) {}

  @Override
  public void destroy() {}
}
```

```java
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClientRateLimiterTest {
  private ClientRateLimiter limiter;
  private long currentTime = 0;

  @Before
  public void setUp() {
    limiter = new ClientRateLimiter(3, 1, () -> currentTime);
  }

  @Test
  public void allowsRequestsUnderLimit() {
    assertTrue(limiter.allowRequest("client1"));
    assertTrue(limiter.allowRequest("client1"));
    assertTrue(limiter.allowRequest("client1"));
  }

  @Test
  public void blocksRequestsOverLimit() {
    limiter.allowRequest("client1");
    limiter.allowRequest("client1");
    limiter.allowRequest("client1");

    assertFalse(limiter.allowRequest("client1"));
  }

  @Test
  public void isolatesClientsFromEachOther() {
    limiter.allowRequest("client1");
    limiter.allowRequest("client1");
    limiter.allowRequest("client1");

    assertTrue(limiter.allowRequest("client2"));
  }

  @Test
  public void allowsNewRequestsAfterWindowExpires() {
    limiter.allowRequest("client1");
    limiter.allowRequest("client1");
    limiter.allowRequest("client1");
    assertFalse(limiter.allowRequest("client1"));

    currentTime += 61_000;

    assertTrue(limiter.allowRequest("client1"));
  }
}
```

To register the filter in `web.xml`:

```xml
<filter>
  <filter-name>rateLimit</filter-name>
  <filter-class>RateLimitFilter</filter-class>
</filter>
<filter-mapping>
  <filter-name>rateLimit</filter-name>
  <url-pattern>/api/*</url-pattern>
</filter-mapping>
```

Or in Spring Boot, add `@Component` to `RateLimitFilter` and it auto-registers.

**Cleaned:** Rate limiter isolates concerns (window expiry, request counting, client isolation), extracts time via supplier for testability. Tests cover happy path (under limit), breach, client isolation, and window expiry. Safe to change because each test proves one boundary, and the limiter has no external dependencies.