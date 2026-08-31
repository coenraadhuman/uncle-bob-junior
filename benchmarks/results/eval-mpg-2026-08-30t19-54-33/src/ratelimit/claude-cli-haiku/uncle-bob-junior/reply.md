I'll provide a rate limiter with thread-safe request tracking and HTTP integration. **Assumptions**: 5 requests per 60 seconds per client, identified by IP address; responses rejected with HTTP 429.

```java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = 60_000;
    
    private final Map<String, Deque<Long>> clientRequests = new ConcurrentHashMap<>();
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequests.computeIfAbsent(clientId, k -> new ConcurrentLinkedDeque<>());
        
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - WINDOW_MILLIS) {
            timestamps.removeFirst();
        }
        
        if (timestamps.size() < MAX_REQUESTS) {
            timestamps.addLast(now);
            return true;
        }
        
        return false;
    }
}
```

```java
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class RateLimitingFilter implements Filter {
    private final RateLimiter limiter = new RateLimiter();
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientId = getClientId(httpRequest);
        
        if (!limiter.allowRequest(clientId)) {
            httpResponse.sendError(429, "Too Many Requests");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private String getClientId(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isEmpty()) ? forwarded : request.getRemoteAddr();
    }
    
    @Override
    public void init(FilterConfig config) {}
    
    @Override
    public void destroy() {}
}
```

```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.*;

class RateLimiterTest {
    private RateLimiter limiter;
    
    @BeforeEach
    void setUp() {
        limiter = new RateLimiter();
    }
    
    @Test
    void allowsRequestsWithinLimit() {
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allowRequest("client1"));
        }
    }
    
    @Test
    void rejectsRequestsExceedingLimit() {
        for (int i = 0; i < 5; i++) {
            limiter.allowRequest("client1");
        }
        
        assertFalse(limiter.allowRequest("client1"));
    }
    
    @Test
    void tracksDifferentClientsIndependently() {
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allowRequest("client1"));
            assertTrue(limiter.allowRequest("client2"));
        }
        
        assertFalse(limiter.allowRequest("client1"));
        assertFalse(limiter.allowRequest("client2"));
    }
    
    @Test
    void handlesConcurrentRequests() throws InterruptedException {
        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());
        
        Runnable fillLimit = () -> {
            for (int i = 0; i < 3; i++) {
                results.add(limiter.allowRequest("client1"));
            }
        };
        
        Thread t1 = new Thread(fillLimit);
        Thread t2 = new Thread(fillLimit);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        long allowed = results.stream().filter(b -> b).count();
        assertEquals(5, allowed);
    }
    
    @Test
    void returnsOldRequestsToPool() throws InterruptedException {
        limiter.allowRequest("client1");
        limiter.allowRequest("client1");
        
        Thread.sleep(10);
        
        limiter.allowRequest("client1");
        limiter.allowRequest("client1");
        limiter.allowRequest("client1");
        
        assertFalse(limiter.allowRequest("client1"));
        
        Thread.sleep(60_010);
        
        assertTrue(limiter.allowRequest("client1"));
    }
}
```

**To integrate**: Register the filter in your servlet container (web.xml or `@Bean` in Spring), or apply it at the framework level. The `RateLimiter` is stateful and thread-safe; share one instance across all requests to a handler.