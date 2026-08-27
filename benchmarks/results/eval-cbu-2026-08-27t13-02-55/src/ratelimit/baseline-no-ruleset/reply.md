I'll provide a rate limiter using a sliding window approach with in-memory tracking by client IP address.

**Assumptions:**
- Rate limit: 5 requests per minute per client
- Client identified by IP address
- Thread-safe for concurrent requests
- In-memory storage (resets on server restart)
- Returns HTTP 429 (Too Many Requests) when limit exceeded

```java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_SIZE_MS = 60_000; // 1 minute
    private static final long CLEANUP_INTERVAL_MS = 60_000;
    
    private final ConcurrentHashMap<String, Queue<Long>> requestTimestamps = 
        new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = 
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "RateLimiter-Cleanup");
            t.setDaemon(true);
            return t;
        });
    
    public RateLimiter() {
        // Clean up old entries periodically
        cleanupExecutor.scheduleAtFixedRate(this::cleanup, 
            CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Queue<Long> timestamps = requestTimestamps.computeIfAbsent(clientId, 
            k -> new ConcurrentLinkedQueue<>());
        
        // Remove timestamps outside the window
        while (!timestamps.isEmpty() && timestamps.peek() < now - WINDOW_SIZE_MS) {
            timestamps.poll();
        }
        
        // Check if limit exceeded
        if (timestamps.size() >= MAX_REQUESTS) {
            return false;
        }
        
        // Add current timestamp
        timestamps.offer(now);
        return true;
    }
    
    private void cleanup() {
        long now = System.currentTimeMillis();
        requestTimestamps.entrySet().removeIf(entry -> {
            Queue<Long> queue = entry.getValue();
            queue.removeIf(timestamp -> timestamp < now - WINDOW_SIZE_MS);
            return queue.isEmpty();
        });
    }
    
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}
```

**Usage with HttpHandler (plain Java HTTP):**

```java
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class RateLimitingHandler implements HttpHandler {
    private final RateLimiter rateLimiter = new RateLimiter();
    private final HttpHandler delegate;
    
    public RateLimitingHandler(HttpHandler delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            exchange.sendResponseHeaders(429, -1);
            exchange.close();
            return;
        }
        
        delegate.handle(exchange);
    }
}
```

**Usage example:**

```java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;

public class Example {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Wrap your handler with rate limiting
        server.createContext("/api", new RateLimitingHandler(exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 2);
            OutputStream os = exchange.getResponseBody();
            os.write("OK".getBytes());
            os.close();
        }));
        
        server.setExecutor(null);
        server.start();
        System.out.println("Server running on port 8080");
    }
}
```

**For Spring Boot (alternative):**

```java
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            response.setHeader("Retry-After", "60");
            response.sendError(429, "Too Many Requests");
            return false;
        }
        
        return true;
    }
}
```

The solution uses a sliding window algorithm that tracks request timestamps per client. Each request within the time window is counted, and excess requests are rejected with HTTP 429. Old entries are automatically cleaned up to prevent memory leaks.