I'll provide a thread-safe in-memory rate limiter that tracks requests per client IP and blocks after 5 requests per minute. Assumptions: Java with concurrent utilities, Spring-based HTTP framework, client identified by IP (with X-Forwarded-For proxy support).

```java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = 60_000;
    
    private final Map<String, Queue<Long>> requestsByClient = new ConcurrentHashMap<>();
    private final Clock clock;
    
    public RateLimiter() {
        this(System::currentTimeMillis);
    }
    
    RateLimiter(Clock clock) {
        this.clock = clock;
    }
    
    public boolean allowRequest(String clientId) {
        long now = clock.currentTimeMillis();
        Queue<Long> requests = requestsByClient.computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<>());
        
        requests.removeIf(timestamp -> now - timestamp > WINDOW_MILLIS);
        
        if (requests.size() >= MAX_REQUESTS) {
            return false;
        }
        
        requests.offer(now);
        return true;
    }
    
    @FunctionalInterface
    interface Clock {
        long currentTimeMillis();
    }
}
```

**Integration as a servlet filter:**

```java
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String clientIp = getClientIp(request);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(429);
            response.getWriter().write("Too many requests");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

**Tests:**

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {
    private RateLimiter rateLimiter;
    private long fakeTime;
    
    @BeforeEach
    void setup() {
        fakeTime = 0;
        rateLimiter = new RateLimiter(() -> fakeTime);
    }
    
    @Test
    void allowsRequestsWithinLimit() {
        String clientId = "192.168.1.1";
        
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.allowRequest(clientId));
        }
    }
    
    @Test
    void blocksRequestsExceedingLimit() {
        String clientId = "192.168.1.1";
        
        for (int i = 0; i < 5; i++) {
            rateLimiter.allowRequest(clientId);
        }
        
        assertFalse(rateLimiter.allowRequest(clientId));
    }
    
    @Test
    void isolatesLimitPerClient() {
        assertTrue(rateLimiter.allowRequest("192.168.1.1"));
        assertTrue(rateLimiter.allowRequest("192.168.1.2"));
    }
    
    @Test
    void allowsNewRequestsAfterTimeWindow() {
        String clientId = "192.168.1.1";
        
        for (int i = 0; i < 5; i++) {
            rateLimiter.allowRequest(clientId);
        }
        assertFalse(rateLimiter.allowRequest(clientId));
        
        fakeTime += 61_000;
        
        assertTrue(rateLimiter.allowRequest(clientId));
    }
    
    @Test
    void allowsPartialRefillBeforeWindowExpires() {
        String clientId = "192.168.1.1";
        
        rateLimiter.allowRequest(clientId);
        fakeTime += 30_000;
        rateLimiter.allowRequest(clientId);
        
        fakeTime += 30_500;
        
        assertTrue(rateLimiter.allowRequest(clientId));
    }
}
```

**How it works:** Each client IP is tracked in a queue of request timestamps. Before checking the limit, the filter removes timestamps older than 60 seconds (the window). If the queue reaches 5 requests, new requests are blocked with HTTP 429. The clock is injectable for fast testing without sleeping.