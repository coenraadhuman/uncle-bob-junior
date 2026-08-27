I'll provide a rate limiter implementation for Java HTTP handlers with per-client tracking.

**Assumptions:**
- Rate limit: 5 requests per minute per client
- Client identification: IP address
- Storage: in-memory (for production, use Redis)
- Framework-agnostic core implementation, plus servlet example

```java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token bucket rate limiter with per-client tracking
 */
public class RateLimiter {
    private static class TokenBucket {
        private double tokens;
        private long lastRefillTime;
        private final double maxTokens;
        private final double refillRate; // tokens per millisecond
        
        TokenBucket(int requestsPerMinute) {
            this.maxTokens = requestsPerMinute;
            this.tokens = requestsPerMinute;
            this.lastRefillTime = System.currentTimeMillis();
            this.refillRate = requestsPerMinute / 60000.0; // per millisecond
        }
        
        boolean allowRequest() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
        
        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;
            tokens = Math.min(maxTokens, tokens + timePassed * refillRate);
            lastRefillTime = now;
        }
    }
    
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final int requestsPerMinute;
    
    public RateLimiter(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
        // Clean up stale entries every 10 minutes
        startCleanupTask();
    }
    
    public boolean isAllowed(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, 
            k -> new TokenBucket(requestsPerMinute));
        return bucket.allowRequest();
    }
    
    private void startCleanupTask() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10 * 60 * 1000); // Every 10 minutes
                    long now = System.currentTimeMillis();
                    buckets.entrySet().removeIf(entry -> 
                        now - entry.getValue().lastRefillTime > 15 * 60 * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }
}
```

**Servlet Example:**

```java
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitingFilter implements Filter {
    private RateLimiter rateLimiter;
    
    @Override
    public void init(FilterConfig config) {
        this.rateLimiter = new RateLimiter(5); // 5 requests per minute
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientId = getClientIp(httpRequest);
        
        if (rateLimiter.isAllowed(clientId)) {
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            httpResponse.setHeader("Retry-After", "60");
            httpResponse.getWriter().write("{\"error\": \"Rate limit exceeded. Max 5 requests per minute.\"}");
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    @Override
    public void destroy() {}
}
```

**Spring Boot Example:**

```java
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter = new RateLimiter(5);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
            Object handler) throws Exception {
        String clientId = getClientIp(request);
        
        if (!rateLimiter.isAllowed(clientId)) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
            return false;
        }
        return true;
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

**Key points:**
- **Token bucket algorithm**: Refills at a steady rate; allows burst up to the limit
- **Per-IP tracking**: Extracts client IP from request, with support for X-Forwarded-For proxies
- **Memory cleanup**: Removes stale entries every 10 minutes to prevent memory leaks
- **HTTP 429**: Returns standard "Too Many Requests" status with Retry-After header
- **Thread-safe**: Uses ConcurrentHashMap for concurrent requests

For production, replace the in-memory `ConcurrentHashMap` with Redis to share state across multiple instances.