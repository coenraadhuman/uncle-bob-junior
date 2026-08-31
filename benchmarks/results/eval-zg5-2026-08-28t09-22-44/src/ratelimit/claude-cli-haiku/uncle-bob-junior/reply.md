I'll provide a thread-safe rate limiter with a 5 requests per minute limit per client IP, using a fixed sliding window approach.

**Assumptions:**
- 5 requests per minute (reasonable interpretation of "a few")
- Client identified by IP address
- Using Java's `HttpHandler` from `com.sun.net.httpserver`
- Return HTTP 429 when limit exceeded

```java
interface Clock {
  long millis();
}

class RateLimiter {
  private static final int MAX_REQUESTS = 5;
  private static final long WINDOW_MILLIS = 60_000;
  
  private final Clock clock;
  private final Map<String, ClientWindow> windows;
  
  RateLimiter() {
    this(() -> System.currentTimeMillis());
  }
  
  RateLimiter(Clock clock) {
    this.clock = clock;
    this.windows = new ConcurrentHashMap<>();
  }
  
  boolean allowRequest(String clientId) {
    long now = clock.millis();
    windows.values().removeIf(w -> w.isExpired(now));
    
    ClientWindow window = windows.computeIfAbsent(clientId, id -> new ClientWindow(now));
    return window.tryRecord(now);
  }
  
  private static class ClientWindow {
    private long windowStart;
    private int count;
    
    ClientWindow(long start) {
      this.windowStart = start;
    }
    
    synchronized boolean tryRecord(long now) {
      if (now - windowStart > WINDOW_MILLIS) {
        windowStart = now;
        count = 0;
      }
      
      if (count < MAX_REQUESTS) {
        count++;
        return true;
      }
      return false;
    }
    
    synchronized boolean isExpired(long now) {
      return now - windowStart > WINDOW_MILLIS;
    }
  }
}

class RateLimitedHandler implements HttpHandler {
  private final RateLimiter limiter;
  
  RateLimitedHandler(RateLimiter limiter) {
    this.limiter = limiter;
  }
  
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String clientId = extractClientIp(exchange);
    
    if (!limiter.allowRequest(clientId)) {
      sendTooManyRequests(exchange);
      return;
    }
    
    handleRequest(exchange);
  }
  
  private String extractClientIp(HttpExchange exchange) {
    InetSocketAddress remote = exchange.getRemoteAddress();
    if (remote == null) {
      return "unknown";
    }
    return remote.getAddress().getHostAddress();
  }
  
  private void sendTooManyRequests(HttpExchange exchange) throws IOException {
    exchange.getResponseHeaders().set("Retry-After", "60");
    exchange.sendResponseHeaders(429, 0);
    exchange.getResponseBody().close();
  }
  
  private void handleRequest(HttpExchange exchange) throws IOException {
    byte[] response = "OK".getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(200, response.length);
    exchange.getResponseBody().write(response);
    exchange.getResponseBody().close();
  }
}

class RateLimiterTest {
  private long time;
  private RateLimiter limiter;
  
  @Before
  public void setUp() {
    time = 1000;
    limiter = new RateLimiter(() -> time);
  }
  
  @Test
  public void allowsRequestsWithinLimit() {
    for (int i = 0; i < 5; i++) {
      assertTrue(limiter.allowRequest("client1"));
    }
  }
  
  @Test
  public void blocksRequestsExceedingLimit() {
    for (int i = 0; i < 5; i++) {
      limiter.allowRequest("client1");
    }
    assertFalse(limiter.allowRequest("client1"));
  }
  
  @Test
  public void resetsAfterWindowExpires() {
    for (int i = 0; i < 5; i++) {
      limiter.allowRequest("client1");
    }
    assertFalse(limiter.allowRequest("client1"));
    
    time += 60_001;
    assertTrue(limiter.allowRequest("client1"));
  }
  
  @Test
  public void isolatesClientsPerIdentifier() {
    for (int i = 0; i < 5; i++) {
      limiter.allowRequest("client1");
      assertTrue(limiter.allowRequest("client2"));
    }
  }
}
```

**Design:** `RateLimiter` tracks requests per client in 60-second windows with automatic cleanup of expired entries. `RateLimitedHandler` applies it by extracting client IP and returning 429 Too Many Requests when the limit is hit. The `Clock` interface enables testable time handling without sleeping in tests.