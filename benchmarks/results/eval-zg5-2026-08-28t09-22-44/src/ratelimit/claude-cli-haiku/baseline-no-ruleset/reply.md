I'll provide a thread-safe rate limiter implementation that tracks requests per client IP with a sliding window approach.

**Assumptions:**
- 5 requests per minute per IP address
- Identify clients by IP address
- Clean up stale entries to prevent memory leaks

```java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static class ClientRequests {
        private final Deque<Long> timestamps = new LinkedList<>();
        
        synchronized boolean allowRequest(int maxRequests, long windowMillis) {
            long now = System.currentTimeMillis();
            
            // Remove timestamps outside the window
            while (!timestamps.isEmpty() && timestamps.peekFirst() < now - windowMillis) {
                timestamps.removeFirst();
            }
            
            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                return true;
            }
            return false;
        }
    }
    
    private final Map<String, ClientRequests> clients = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long windowMillis;
    
    public RateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        
        // Cleanup thread for stale entries (removes clients with no requests in 10 minutes)
        new Timer(true).scheduleAtFixedRate(() -> {
            long cutoff = System.currentTimeMillis() - 600_000;
            clients.entrySet().removeIf(e -> {
                Deque<Long> timestamps = e.getValue().timestamps;
                synchronized (e.getValue()) {
                    return timestamps.isEmpty() || timestamps.peekLast() < cutoff;
                }
            });
        }, 60_000, 60_000);
    }
    
    public boolean allowRequest(String clientId) {
        return clients.computeIfAbsent(clientId, k -> new ClientRequests())
                .allowRequest(maxRequests, windowMillis);
    }
}
```

**Example with a servlet:**

```java
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitFilter implements Filter {
    private RateLimiter rateLimiter;
    
    @Override
    public void init(FilterConfig config) {
        // 5 requests per minute (60,000 milliseconds)
        rateLimiter = new RateLimiter(5, 60_000);
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        String clientIp = request.getRemoteAddr();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            httpResponse.setHeader("Retry-After", "60");
            response.getWriter().write("Rate limit exceeded");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {}
}
```

**Example with Spring:**

```java
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter = new RateLimiter(5, 60_000);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                            Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setHeader("Retry-After", "60");
            response.getWriter().write("Rate limit exceeded");
            return false;
        }
        
        return true;
    }
}
```

The rate limiter uses a sliding window with a `Deque` to track request timestamps per client, automatically cleaning up stale entries every minute. Adjust `maxRequests` and `windowMillis` as needed.