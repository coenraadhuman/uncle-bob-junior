I'll provide a clean, configurable rate limiter with tests. Assumptions: Java 8+, 10 requests per minute default limit per client, client ID derived from IP address (with X-Forwarded-For support).

```java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int DEFAULT_MAX_REQUESTS = 10;
    private static final long DEFAULT_WINDOW_MILLIS = 60_000;
    
    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Deque<Long>> clientTimestamps = 
        new ConcurrentHashMap<>();
    
    public RateLimiter() {
        this(DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_MILLIS);
    }
    
    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }
    
    public boolean isAllowed(String clientId) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowMillis;
        
        Deque<Long> timestamps = clientTimestamps.computeIfAbsent(
            clientId, 
            k -> new ConcurrentLinkedDeque<>()
        );
        
        timestamps.removeIf(ts -> ts < windowStart);
        
        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }
}
```

```java
import javax.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    private static final RateLimiter rateLimiter = new RateLimiter();
    private static final String RATE_LIMIT_EXCEEDED = "Too many requests";
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
            HttpServletResponse response, Object handler) throws Exception {
        String clientId = extractClientId(request);
        
        if (!rateLimiter.isAllowed(clientId)) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"" + RATE_LIMIT_EXCEEDED + "\"}");
            return false;
        }
        return true;
    }
    
    private String extractClientId(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
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
        rateLimiter = new RateLimiter(3, 60_000);
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
    public void treatsDifferentClientsIndependently() {
        for (int i = 0; i < 3; i++) {
            rateLimiter.isAllowed("client1");
        }
        assertTrue(rateLimiter.isAllowed("client2"));
    }
    
    @Test
    public void allowsNewRequestsAfterWindowExpires() throws InterruptedException {
        RateLimiter quickLimiter = new RateLimiter(1, 100);
        assertTrue(quickLimiter.isAllowed("client1"));
        assertFalse(quickLimiter.isAllowed("client1"));
        Thread.sleep(101);
        assertTrue(quickLimiter.isAllowed("client1"));
    }
}
```

Register the interceptor in your Spring configuration:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitingInterceptor());
    }
}
```

**Cleaned:** Per-client sliding-window rate limiter with configurable limits; guard clause for rate-limit check; client ID extracted from X-Forwarded-For or IP; no magic constants. **Safe to change because:** RateLimiter has one responsibility (enforcing limits per client), tests cover limit enforcement, expiry, and per-client isolation.