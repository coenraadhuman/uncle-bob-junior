I'll provide a rate limiter using a token bucket approach with IP-based client identification. Here's a complete, thread-safe implementation:

```java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static class ClientQuota {
        private double tokens;
        private long lastRefillTime;
        
        ClientQuota(int capacity) {
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }
    }
    
    private final int capacity;
    private final int refillRatePerMinute;
    private final ConcurrentHashMap<String, ClientQuota> quotas;
    private final long refillIntervalMs;
    
    public RateLimiter(int requestsPerMinute) {
        this.capacity = requestsPerMinute;
        this.refillRatePerMinute = requestsPerMinute;
        this.quotas = new ConcurrentHashMap<>();
        this.refillIntervalMs = 60_000L / requestsPerMinute;
    }
    
    public synchronized boolean allowRequest(String clientId) {
        ClientQuota quota = quotas.computeIfAbsent(clientId, k -> new ClientQuota(capacity));
        
        long now = System.currentTimeMillis();
        long timePassed = now - quota.lastRefillTime;
        
        quota.tokens += (double) timePassed / refillIntervalMs;
        if (quota.tokens > capacity) {
            quota.tokens = capacity;
        }
        quota.lastRefillTime = now;
        
        if (quota.tokens >= 1.0) {
            quota.tokens -= 1.0;
            return true;
        }
        return false;
    }
    
    public void cleanup() {
        long now = System.currentTimeMillis();
        quotas.entrySet().removeIf(entry -> 
            (now - entry.getValue().lastRefillTime) > 300_000
        );
    }
}
```

**Usage with Java HttpServer:**

```java
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;

public class RateLimitedHttpServer {
    public static void main(String[] args) throws IOException {
        RateLimiter rateLimiter = new RateLimiter(5); // 5 requests per minute
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/api", exchange -> {
            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
            
            if (!rateLimiter.allowRequest(clientIp)) {
                exchange.getResponseHeaders().set("Retry-After", "12");
                exchange.sendResponseHeaders(429, 0);
                exchange.getResponseBody().close();
                return;
            }
            
            String response = "Success";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        });
        
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server running on port 8080");
        
        // Cleanup task
        ScheduledExecutorService cleanup = Executors.newScheduledThreadPool(1);
        cleanup.scheduleAtFixedRate(rateLimiter::cleanup, 5, 5, TimeUnit.MINUTES);
    }
}
```

**Or as a Servlet Filter (for Spring/Jakarta):**

```java
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitFilter implements Filter {
    private RateLimiter rateLimiter;
    
    @Override
    public void init(FilterConfig config) {
        this.rateLimiter = new RateLimiter(5); // 5 requests per minute
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        String clientIp = request.getRemoteAddr();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setHeader("Retry-After", "12");
            httpResponse.sendError(429, "Too Many Requests");
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

**Assumptions made:**
- Rate limit of 5 requests per minute (configurable in constructor)
- Client identification by IP address
- Returns HTTP 429 (Too Many Requests) when limit exceeded
- Token bucket algorithm for smooth rate limiting
- Automatic cleanup of stale entries every 5 minutes
- Thread-safe for concurrent requests