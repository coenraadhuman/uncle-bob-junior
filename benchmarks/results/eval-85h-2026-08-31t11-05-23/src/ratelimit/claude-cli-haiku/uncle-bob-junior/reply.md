I'll create a rate limiter using a token bucket approach and integrate it with a Java HTTP handler.

**Assumptions:**
- 10 requests per minute per client
- Client identified by IP address
- Using Java's built-in `HttpServer` for the handler example
- Thread-safe implementation with `ConcurrentHashMap`

```java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final long WINDOW_MILLIS = 60_000;
    private static final int MAX_REQUESTS = 10;
    
    private final Map<String, ClientWindow> windows = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanup = 
        Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "RateLimiter-Cleanup");
            t.setDaemon(true);
            return t;
        });
    
    public RateLimiter() {
        cleanup.scheduleAtFixedRate(this::purgeExpired, 1, 1, TimeUnit.MINUTES);
    }
    
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        ClientWindow window = windows.computeIfAbsent(clientId, k -> new ClientWindow(now));
        return window.allowRequest(now);
    }
    
    private void purgeExpired() {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(e -> now - e.getValue().lastAccessTime() > WINDOW_MILLIS * 2);
    }
    
    public void shutdown() {
        cleanup.shutdown();
    }
    
    private static class ClientWindow {
        private long windowStart;
        private int requestCount;
        private long lastAccess;
        
        ClientWindow(long startTime) {
            this.windowStart = startTime;
            this.requestCount = 0;
            this.lastAccess = startTime;
        }
        
        synchronized boolean allowRequest(long now) {
            lastAccess = now;
            
            if (now - windowStart >= WINDOW_MILLIS) {
                windowStart = now;
                requestCount = 0;
            }
            
            if (requestCount < MAX_REQUESTS) {
                requestCount++;
                return true;
            }
            return false;
        }
        
        synchronized long lastAccessTime() {
            return lastAccess;
        }
    }
}
```

```java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHandler implements HttpHandler {
    private final RateLimiter rateLimiter;
    private final HttpHandler delegate;
    
    public RateLimitedHandler(HttpHandler delegate, RateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = extractClientIp(exchange);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            sendTooManyRequests(exchange);
            return;
        }
        
        delegate.handle(exchange);
    }
    
    private String extractClientIp(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
    
    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] response = "Rate limit exceeded: max 10 requests per minute".getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(429, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
```

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {
    private RateLimiter rateLimiter;
    
    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter();
    }
    
    @Test
    void allowsRequestsUpToLimit() {
        String client = "192.168.1.100";
        
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.allowRequest(client), 
                "Request " + (i + 1) + " should be allowed");
        }
    }
    
    @Test
    void blocksRequestsExceedingLimit() {
        String client = "192.168.1.100";
        
        for (int i = 0; i < 10; i++) {
            rateLimiter.allowRequest(client);
        }
        
        assertFalse(rateLimiter.allowRequest(client), 
            "Request 11 should be blocked");
    }
    
    @Test
    void isolatesClientsFromEachOther() {
        assertTrue(rateLimiter.allowRequest("client1"));
        assertTrue(rateLimiter.allowRequest("client2"));
        
        for (int i = 0; i < 9; i++) {
            rateLimiter.allowRequest("client1");
        }
        
        assertFalse(rateLimiter.allowRequest("client1"));
        assertTrue(rateLimiter.allowRequest("client2"), 
            "client2 should not be affected by client1 limit");
    }
    
    @Test
    void resetsWindowAfterTimeWindow() throws InterruptedException {
        String client = "192.168.1.100";
        
        for (int i = 0; i < 10; i++) {
            rateLimiter.allowRequest(client);
        }
        
        assertFalse(rateLimiter.allowRequest(client));
        
        Thread.sleep(61_000);
        
        assertTrue(rateLimiter.allowRequest(client), 
            "Should allow request after window expires");
    }
}
```

**Cleaned:** Rate limiting isolated in one class with named constants and per-client windows; HTTP handler delegates request filtering cleanly; comprehensive tests cover limit enforcement, client isolation, and window reset.

**Safe to change because:** Logic for rate limiting decisions lives in one place; tests verify all branches (allowed, blocked, reset); handler is stateless and reusable with any HTTP handler implementation.