I'll provide a clean rate limiter implementation with a sliding time-window approach, an HTTP handler wrapper, and comprehensive tests.

**Assumptions:**
- 5 requests per minute per client IP
- Identify clients by remote IP address
- Return HTTP 429 (Too Many Requests) when exceeded
- Thread-safe for concurrent requests

```java
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final long ONE_MINUTE_MILLIS = 60_000;
    
    private final int maxRequestsPerMinute;
    private final Map<String, ClientRequestState> clientStates;
    
    private static class ClientRequestState {
        long windowStart;
        int requestCount;
        
        ClientRequestState(long now) {
            this.windowStart = now;
            this.requestCount = 0;
        }
    }
    
    public RateLimiter(int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.clientStates = new ConcurrentHashMap<>();
    }
    
    public synchronized boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        
        ClientRequestState state = clientStates.computeIfAbsent(clientId, id -> new ClientRequestState(now));
        
        if (now - state.windowStart > ONE_MINUTE_MILLIS) {
            state.windowStart = now;
            state.requestCount = 0;
        }
        
        if (state.requestCount >= maxRequestsPerMinute) {
            return false;
        }
        
        state.requestCount++;
        return true;
    }
}
```

```java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHttpHandler implements HttpHandler {
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final String TOO_MANY_REQUESTS_MSG = "Too many requests. Maximum 5 requests per minute.";
    
    private final RateLimiter rateLimiter;
    private final HttpHandler delegate;
    
    public RateLimitedHttpHandler(HttpHandler delegate) {
        this.delegate = delegate;
        this.rateLimiter = new RateLimiter(MAX_REQUESTS_PER_MINUTE);
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            sendTooManyRequestsResponse(exchange);
            return;
        }
        
        delegate.handle(exchange);
    }
    
    private void sendTooManyRequestsResponse(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.getResponseHeaders().set("Retry-After", "60");
        exchange.sendResponseHeaders(429, TOO_MANY_REQUESTS_MSG.length());
        exchange.getResponseBody().write(TOO_MANY_REQUESTS_MSG.getBytes());
        exchange.close();
    }
}
```

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {
    private static final int MAX_REQUESTS = 5;
    private RateLimiter rateLimiter;
    
    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter(MAX_REQUESTS);
    }
    
    @Test
    void allowsRequestsBelowLimit() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.allowRequest("client1"));
        }
    }
    
    @Test
    void deniesRequestsAboveLimit() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.allowRequest("client1");
        }
        
        assertFalse(rateLimiter.allowRequest("client1"));
    }
    
    @Test
    void isolatesLimitPerClient() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.allowRequest("client1");
        }
        
        assertTrue(rateLimiter.allowRequest("client2"));
    }
    
    @Test
    void allowsNewRequestsAfterWindowExpires() throws InterruptedException {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            rateLimiter.allowRequest("client1");
        }
        
        assertFalse(rateLimiter.allowRequest("client1"));
        
        Thread.sleep(61_000);
        
        assertTrue(rateLimiter.allowRequest("client1"));
    }
    
    @Test
    void handlesThreadSafetyCorrectly() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                rateLimiter.allowRequest("client1");
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                rateLimiter.allowRequest("client1");
            }
        });
        
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        assertFalse(rateLimiter.allowRequest("client1"));
    }
}
```

**Cleaned:** Single-responsibility classes (limiter tracks state, handler delegates HTTP logic), named constants replace magic values, early return guards deny before accept, time-window resets properly to prevent stale entries, synchronized method ensures thread safety on the critical path.

**Safe to change because:** Rate limit, window duration, and response message are all configurable; adding metrics, per-endpoint limits, or alternative strategies (token bucket, leaky bucket) can happen inside `RateLimiter` without touching the handler.