I'll provide a complete rate-limiting solution for Java HTTP handlers. This implementation tracks requests per client IP and allows 5 requests per minute.

**Assumptions:**
- Spring Boot/Spring Web framework (standard for Java HTTP handling)
- Rate limit: 5 requests per minute per client IP
- Tracks X-Forwarded-For header for proxied requests

```java
// ClientRateLimiter.java
import java.util.*;
import java.util.concurrent.*;

public class ClientRateLimiter {
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = 60_000;
    
    private final Map<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();
    private final Supplier<Long> timeProvider;

    public ClientRateLimiter() {
        this(System::currentTimeMillis);
    }

    ClientRateLimiter(Supplier<Long> timeProvider) {
        this.timeProvider = timeProvider;
    }

    public boolean isAllowed(String clientIp) {
        long now = timeProvider.get();
        Deque<Long> timestamps = requestTimestamps.computeIfAbsent(clientIp, k -> new ConcurrentLinkedDeque<>());
        removeExpired(timestamps, now);
        
        if (timestamps.size() < MAX_REQUESTS) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }

    private void removeExpired(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - WINDOW_MILLIS) {
            timestamps.removeFirst();
        }
    }

    int remaining(String clientIp) {
        long now = timeProvider.get();
        Deque<Long> timestamps = requestTimestamps.get(clientIp);
        if (timestamps == null) {
            return MAX_REQUESTS;
        }
        removeExpired(timestamps, now);
        return Math.max(0, MAX_REQUESTS - timestamps.size());
    }
}
```

```java
// RateLimitingInterceptor.java
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    private static final String RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
    private static final int TOO_MANY_REQUESTS = 429;
    
    private final ClientRateLimiter rateLimiter;

    public RateLimitingInterceptor(ClientRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws IOException {
        String clientIp = extractClientIp(request);
        
        if (!rateLimiter.isAllowed(clientIp)) {
            response.setStatus(TOO_MANY_REQUESTS);
            response.getWriter().write("Rate limit exceeded");
            return false;
        }
        
        response.addHeader(RATE_LIMIT_REMAINING, String.valueOf(rateLimiter.remaining(clientIp)));
        return true;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

```java
// WebConfig.java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private RateLimitingInterceptor rateLimitingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor);
    }
}
```

```java
// ClientRateLimiterTest.java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientRateLimiterTest {
    private ClientRateLimiter rateLimiter;
    private long fakeTime;

    @BeforeEach
    void setUp() {
        fakeTime = 0;
        rateLimiter = new ClientRateLimiter(() -> fakeTime);
    }

    @Test
    void allowsUpToMaxRequests() {
        String clientIp = "192.168.1.1";
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.isAllowed(clientIp));
        }
    }

    @Test
    void blocksExcessRequests() {
        String clientIp = "192.168.1.1";
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed(clientIp);
        }
        assertFalse(rateLimiter.isAllowed(clientIp));
    }

    @Test
    void isolatesPerClient() {
        assertTrue(rateLimiter.isAllowed("192.168.1.1"));
        assertTrue(rateLimiter.isAllowed("192.168.1.2"));
        for (int i = 1; i < 5; i++) {
            rateLimiter.isAllowed("192.168.1.1");
        }
        assertFalse(rateLimiter.isAllowed("192.168.1.1"));
        assertTrue(rateLimiter.isAllowed("192.168.1.2"));
    }

    @Test
    void resetsAfterWindowExpires() {
        String clientIp = "192.168.1.1";
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed(clientIp);
        }
        assertFalse(rateLimiter.isAllowed(clientIp));
        
        fakeTime += 60_001;
        
        assertTrue(rateLimiter.isAllowed(clientIp));
    }

    @Test
    void tracksRemaining() {
        String clientIp = "192.168.1.1";
        assertEquals(5, rateLimiter.remaining(clientIp));
        rateLimiter.isAllowed(clientIp);
        assertEquals(4, rateLimiter.remaining(clientIp));
    }
}
```

Cleaned: rate limiter tracks requests per IP in a sliding window using a concurrent deque for O(1) removal of old requests; interceptor applies the limit and reports remaining quota. Safe to change because the time provider is injectable for testing, and the tracking logic is isolated from the HTTP layer.