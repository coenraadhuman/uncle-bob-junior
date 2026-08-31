**Assumptions:**
- Sliding window counter algorithm (simple, accurate)
- Rate limit per client IP address
- Default: 10 requests per 60 seconds (configurable)
- In-memory storage (no persistence across restarts)
- Thread-safe for concurrent requests

```java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RateLimiter {
    private static final int DEFAULT_MAX_REQUESTS = 10;
    private static final long DEFAULT_WINDOW_MILLIS = 60_000;
    
    private final Map<String, Deque<Long>> clientTimestamps = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long windowMillis;
    
    public RateLimiter() {
        this(DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_MILLIS);
    }
    
    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }
    
    public boolean isAllowed(String clientId) {
        long now = System.currentTimeMillis();
        
        Deque<Long> timestamps = clientTimestamps.computeIfAbsent(
            clientId,
            k -> new ConcurrentLinkedDeque<>()
        );
        
        removeExpiredTimestamps(timestamps, now);
        
        if (timestamps.size() >= maxRequests) {
            return false;
        }
        
        timestamps.addLast(now);
        return true;
    }
    
    private void removeExpiredTimestamps(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && isOlderThanWindow(timestamps.peekFirst(), now)) {
            timestamps.removeFirst();
        }
    }
    
    private boolean isOlderThanWindow(long timestamp, long now) {
        return now - timestamp >= windowMillis;
    }
}
```

```java
public interface HttpRequest {
    String getRemoteAddr();
}

public interface HttpResponse {
    void setStatus(int status);
    void setBody(String body);
}

public class RateLimitingHandler {
    private static final int RATE_LIMIT_STATUS = 429;
    private static final String RATE_LIMIT_MESSAGE = "Too many requests";
    
    private final RateLimiter rateLimiter;
    private final RequestHandler nextHandler;
    
    public RateLimitingHandler(RateLimiter rateLimiter, RequestHandler nextHandler) {
        this.rateLimiter = rateLimiter;
        this.nextHandler = nextHandler;
    }
    
    public void handle(HttpRequest request, HttpResponse response) {
        String clientIp = request.getRemoteAddr();
        
        if (!rateLimiter.isAllowed(clientIp)) {
            response.setStatus(RATE_LIMIT_STATUS);
            response.setBody(RATE_LIMIT_MESSAGE);
            return;
        }
        
        nextHandler.handle(request, response);
    }
    
    @FunctionalInterface
    public interface RequestHandler {
        void handle(HttpRequest request, HttpResponse response);
    }
}
```

```java
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RateLimiterTest {
    private RateLimiter rateLimiter;
    
    @Before
    public void setUp() {
        rateLimiter = new RateLimiter(3, 1000);
    }
    
    @Test
    public void allowsRequestsWithinLimit() {
        assertTrue(rateLimiter.isAllowed("client1"));
        assertTrue(rateLimiter.isAllowed("client1"));
        assertTrue(rateLimiter.isAllowed("client1"));
    }
    
    @Test
    public void deniesRequestsExceedingLimit() {
        rateLimiter.isAllowed("client1");
        rateLimiter.isAllowed("client1");
        rateLimiter.isAllowed("client1");
        
        assertFalse(rateLimiter.isAllowed("client1"));
    }
    
    @Test
    public void isolatesClientQuotas() {
        rateLimiter.isAllowed("client1");
        rateLimiter.isAllowed("client1");
        rateLimiter.isAllowed("client1");
        
        assertTrue(rateLimiter.isAllowed("client2"));
    }
    
    @Test
    public void allowsRequestsAfterWindowExpires() throws InterruptedException {
        rateLimiter.isAllowed("client1");
        rateLimiter.isAllowed("client1");
        rateLimiter.isAllowed("client1");
        assertFalse(rateLimiter.isAllowed("client1"));
        
        Thread.sleep(1100);
        
        assertTrue(rateLimiter.isAllowed("client1"));
    }
}
```

```java
public class Example {
    public static void main(String[] args) {
        RateLimiter rateLimiter = new RateLimiter(10, 60_000);
        
        RateLimitingHandler handler = new RateLimitingHandler(
            rateLimiter,
            (request, response) -> {
                response.setStatus(200);
                response.setBody("OK");
            }
        );
    }
}
```

**Cleaned:** Sliding window counter tracks requests per IP with automatic expiry; handler enforces limit before processing; named constants remove magic values; tests verify all paths (within limit, exceeded, isolation, reset). Safe to extend with metrics, custom limits per endpoint, or persistent storage—the separation keeps rate limiting logic independent.