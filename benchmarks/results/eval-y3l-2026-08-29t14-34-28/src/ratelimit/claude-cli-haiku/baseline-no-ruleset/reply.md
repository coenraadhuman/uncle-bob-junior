I'll provide a complete rate limiting solution for a Java HTTP handler. I'm assuming:
- You're using the JDK's `com.sun.net.httpserver.HttpHandler`
- Rate limit of 10 requests per minute per client (identified by IP address)
- Thread-safe implementation with automatic cleanup of stale entries

```java
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private static final long WINDOW_MILLIS = 60_000; // 1 minute
    private static final int MAX_REQUESTS = 10;
    
    private final Map<String, ClientQuota> quotas = new ConcurrentHashMap<>();
    private final long cleanupIntervalMillis = 120_000; // 2 minutes
    private volatile long lastCleanup = System.currentTimeMillis();
    
    public boolean allowRequest(String clientIp) {
        cleanupIfNeeded();
        ClientQuota quota = quotas.compute(clientIp, (ip, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new ClientQuota();
            }
            return existing;
        });
        return quota.tryConsume();
    }
    
    private void cleanupIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < cleanupIntervalMillis) {
            return;
        }
        synchronized (this) {
            now = System.currentTimeMillis();
            if (now - lastCleanup < cleanupIntervalMillis) {
                return;
            }
            quotas.entrySet().removeIf(e -> e.getValue().isExpired());
            lastCleanup = now;
        }
    }
    
    private static class ClientQuota {
        private long windowStartMillis = System.currentTimeMillis();
        private int requestCount = 0;
        
        synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            if (now - windowStartMillis > WINDOW_MILLIS) {
                windowStartMillis = now;
                requestCount = 0;
            }
            if (requestCount < MAX_REQUESTS) {
                requestCount++;
                return true;
            }
            return false;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - windowStartMillis > WINDOW_MILLIS;
        }
    }
}
```

```java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHandler implements HttpHandler {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = getClientIp(exchange);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            exchange.sendResponseHeaders(429, 0);
            exchange.getResponseBody().close();
            return;
        }
        
        // Handle the actual request
        String response = "OK";
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
    
    private String getClientIp(HttpExchange exchange) {
        String ip = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            return ip.split(",")[0].trim();
        }
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}
```

**Key features:**
- Thread-safe with `ConcurrentHashMap` and synchronized windows
- Sliding window per client IP (resets every minute)
- Auto-cleanup of expired quotas to prevent memory leaks
- Returns HTTP 429 (Too Many Requests) with `Retry-After` header when limit exceeded
- Handles `X-Forwarded-For` header for proxied requests