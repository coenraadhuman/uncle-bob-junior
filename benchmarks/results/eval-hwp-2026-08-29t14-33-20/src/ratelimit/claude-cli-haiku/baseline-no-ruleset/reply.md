I'll provide a thread-safe rate limiter for HTTP handlers that tracks requests per client IP and enforces a limit per minute.

**Assumptions:**
- Rate limit: 5 requests per minute per client
- Client identified by IP address
- Sliding window approach (requests in the last 60 seconds)
- Automatic cleanup of stale entries

```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RateLimiter {
    private final int maxRequests;
    private final long windowMs;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Long>> requestTimestamps;

    public RateLimiter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
        this.requestTimestamps = new ConcurrentHashMap<>();
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        CopyOnWriteArrayList<Long> timestamps = requestTimestamps.computeIfAbsent(
            clientId,
            k -> new CopyOnWriteArrayList<>()
        );

        timestamps.removeIf(timestamp -> now - timestamp > windowMs);

        if (timestamps.size() < maxRequests) {
            timestamps.add(now);
            return true;
        }
        return false;
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        requestTimestamps.forEach((clientId, timestamps) -> {
            timestamps.removeIf(timestamp -> now - timestamp > windowMs);
            if (timestamps.isEmpty()) {
                requestTimestamps.remove(clientId);
            }
        });
    }
}
```

**Example integration with an HTTP servlet:**

```java
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RateLimitingFilter extends HttpFilter {
    private static final RateLimiter rateLimiter = new RateLimiter(5, 60_000);

    static {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, runnable -> {
            Thread thread = new Thread(runnable, "RateLimiter-Cleanup");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(rateLimiter::cleanup, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response,
                           FilterChain chain) throws IOException, ServletException {
        String clientIp = getClientIp(request);

        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Max 5 per minute.\"}");
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

**For Spring Boot controllers:**

```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ApiController {
    private static final RateLimiter rateLimiter = new RateLimiter(5, 60_000);

    @GetMapping("/api/data")
    public ResponseEntity<?> getData(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            return ResponseEntity
                .status(429)
                .body("{\"error\":\"Rate limit exceeded. Max 5 requests per minute.\"}");
        }

        return ResponseEntity.ok("{\"data\":\"your response here\"}");
    }
}
```

The rate limiter uses a sliding window to track the last 60 seconds of requests per client. Requests beyond the limit receive HTTP 429 (Too Many Requests).