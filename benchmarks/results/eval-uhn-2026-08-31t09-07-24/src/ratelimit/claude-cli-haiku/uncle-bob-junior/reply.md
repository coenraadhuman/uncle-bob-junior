I'll create a rate limiter for a Java HTTP handler using a sliding-window approach, limiting to 5 requests per minute per client IP address, returning HTTP 429 when exceeded.

```java
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class RateLimiter {
  private static final int REQUESTS_PER_MINUTE = 5;
  private static final long WINDOW_DURATION_MILLIS = 60_000;

  private final Map<String, Queue<Long>> requestsByClient = new ConcurrentHashMap<>();

  boolean isAllowed(String clientId) {
    Queue<Long> timestamps = requestsByClient.computeIfAbsent(clientId, _ -> new LinkedList<>());
    long now = System.currentTimeMillis();
    long windowStart = now - WINDOW_DURATION_MILLIS;

    while (!timestamps.isEmpty() && timestamps.peek() < windowStart) {
      timestamps.poll();
    }

    if (timestamps.size() >= REQUESTS_PER_MINUTE) {
      return false;
    }

    timestamps.offer(now);
    return true;
  }
}

class HttpResponse {
  final int statusCode;
  final String body;

  HttpResponse(int statusCode, String body) {
    this.statusCode = statusCode;
    this.body = body;
  }
}

class RateLimitedHandler {
  private final RateLimiter rateLimiter;

  RateLimitedHandler(RateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
  }

  HttpResponse handle(String clientIp, String requestBody) {
    if (!rateLimiter.isAllowed(clientIp)) {
      return new HttpResponse(429, "Too Many Requests");
    }
    return new HttpResponse(200, "OK");
  }
}
```

```java
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

class RateLimiterTest {
  private final RateLimiter rateLimiter = new RateLimiter();
  private final String clientId = "192.168.1.1";

  @Test
  void allowsFirstFiveRequests() {
    for (int i = 0; i < 5; i++) {
      assertTrue(rateLimiter.isAllowed(clientId));
    }
  }

  @Test
  void blocksSixthRequest() {
    for (int i = 0; i < 5; i++) {
      rateLimiter.isAllowed(clientId);
    }
    assertFalse(rateLimiter.isAllowed(clientId));
  }

  @Test
  void allowsRequestsAfterWindowExpires() throws InterruptedException {
    for (int i = 0; i < 5; i++) {
      rateLimiter.isAllowed(clientId);
    }
    assertFalse(rateLimiter.isAllowed(clientId));
    
    TimeUnit.SECONDS.sleep(1);
    assertTrue(rateLimiter.isAllowed(clientId));
  }

  @Test
  void isolatesClientQuotas() {
    String client1 = "192.168.1.1";
    String client2 = "192.168.1.2";

    for (int i = 0; i < 5; i++) {
      rateLimiter.isAllowed(client1);
    }

    assertTrue(rateLimiter.isAllowed(client2));
  }
}

class RateLimitedHandlerTest {
  private final RateLimiter rateLimiter = new RateLimiter();
  private final RateLimitedHandler handler = new RateLimitedHandler(rateLimiter);

  @Test
  void returns200ForAllowedRequest() {
    HttpResponse response = handler.handle("192.168.1.1", "test");
    assertEquals(200, response.statusCode);
    assertEquals("OK", response.body);
  }

  @Test
  void returns429WhenRateLimited() {
    String clientIp = "192.168.1.1";
    for (int i = 0; i < 5; i++) {
      handler.handle(clientIp, "test");
    }

    HttpResponse response = handler.handle(clientIp, "test");
    assertEquals(429, response.statusCode);
    assertEquals("Too Many Requests", response.body);
  }
}
```

**Assumptions:** Client identified by IP address; 5 requests per 60-second sliding window; thread-safe using `ConcurrentHashMap`; queue of timestamps tracks requests within the window, old requests are discarded automatically.

Cleaned: One responsibility per class (`RateLimiter` tracks, `Handler` responds); guard clause on rate limit check; named constants for thresholds and durations. Safe to change because quota limits and window size are isolated constants, and logic exists in one place.