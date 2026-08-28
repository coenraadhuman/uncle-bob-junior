I'll provide a clean rate limiter implementation with a servlet filter and comprehensive tests.

**Assumptions:**
- Servlet-based Java framework (Spring, vanilla servlets, etc.)
- 10 requests per minute per client (adjust `MAX_REQUESTS` as needed)
- Client identified by IP address
- Return HTTP 429 (Too Many Requests) when exceeded

```java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MILLIS = 60_000; // 1 minute
    
    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, ClientRequestHistory> history;
    
    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.history = new ConcurrentHashMap<>();
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        ClientRequestHistory clientHistory = history.computeIfAbsent(clientId,
            _ -> new ClientRequestHistory());
        
        return clientHistory.recordRequest(now, maxRequests, windowMillis);
    }
    
    private static class ClientRequestHistory {
        private final Queue<Long> timestamps = new ConcurrentLinkedQueue<>();
        
        synchronized boolean recordRequest(long now, int maxRequests, long windowMillis) {
            timestamps.removeIf(ts -> now - ts > windowMillis);
            
            if (timestamps.size() >= maxRequests) {
                return false;
            }
            
            timestamps.offer(now);
            return true;
        }
    }
}
```

```java
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class RateLimitFilter implements Filter {
    private RateLimiter rateLimiter;
    
    @Override
    public void init(FilterConfig config) {
        rateLimiter = new RateLimiter(10, 60_000);
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientId = httpRequest.getRemoteAddr();
        
        if (!rateLimiter.allowRequest(clientId)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(429, "Too Many Requests");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {}
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {
    private static final String CLIENT = "192.168.1.1";
    
    @Test
    void allowsRequestsUnderLimit() {
        RateLimiter limiter = new RateLimiter(3, 60_000);
        
        assertTrue(limiter.allowRequest(CLIENT));
        assertTrue(limiter.allowRequest(CLIENT));
        assertTrue(limiter.allowRequest(CLIENT));
    }
    
    @Test
    void rejectsRequestsOverLimit() {
        RateLimiter limiter = new RateLimiter(3, 60_000);
        
        assertTrue(limiter.allowRequest(CLIENT));
        assertTrue(limiter.allowRequest(CLIENT));
        assertTrue(limiter.allowRequest(CLIENT));
        assertFalse(limiter.allowRequest(CLIENT));
    }
    
    @Test
    void resetAfterWindowExpires() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(2, 100); // 100ms window
        
        assertTrue(limiter.allowRequest(CLIENT));
        assertTrue(limiter.allowRequest(CLIENT));
        assertFalse(limiter.allowRequest(CLIENT));
        
        Thread.sleep(110);
        assertTrue(limiter.allowRequest(CLIENT));
    }
    
    @Test
    void isolatesLimitPerClient() {
        RateLimiter limiter = new RateLimiter(2, 60_000);
        
        assertTrue(limiter.allowRequest("client1"));
        assertTrue(limiter.allowRequest("client1"));
        assertFalse(limiter.allowRequest("client1"));
        
        assertTrue(limiter.allowRequest("client2"));
        assertTrue(limiter.allowRequest("client2"));
        assertFalse(limiter.allowRequest("client2"));
    }
}
```

**To register the filter in Spring Boot**, add to your configuration:
```java
@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new RateLimitFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(1);
        return bean;
    }
}
```

**Code structure:** RateLimiter tracks timestamps per client in a queue, discarding old entries outside the window. The filter intercepts every request, identifies the client by IP, and rejects with HTTP 429 if the quota is exceeded. Tests verify the happy path, boundary, window expiry, and per-client isolation.