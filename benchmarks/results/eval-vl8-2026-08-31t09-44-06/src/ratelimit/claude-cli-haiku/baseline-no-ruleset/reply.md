I'll provide a rate limiter for Java HTTP handlers. Here are my assumptions:
- Rate limit: 10 requests per minute per client IP
- Track clients by IP address
- Use an in-memory sliding window approach
- Clean up expired entries automatically

```java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static final int REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_MS = 60_000;
    
    private final ConcurrentHashMap<String, Deque<Long>> clientRequests = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newScheduledThreadPool(1);
    
    public RateLimiter() {
        cleaner.scheduleAtFixedRate(this::cleanupExpired, 1, 1, TimeUnit.MINUTES);
    }
    
    public boolean allowRequest(String clientIp) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = clientRequests.computeIfAbsent(clientIp, k -> new ConcurrentLinkedDeque<>());
        
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                timestamps.removeFirst();
            }
            
            if (timestamps.size() < REQUESTS_PER_MINUTE) {
                timestamps.addLast(now);
                return true;
            }
            return false;
        }
    }
    
    private void cleanupExpired() {
        long threshold = System.currentTimeMillis() - WINDOW_MS;
        clientRequests.forEach((ip, timestamps) -> {
            synchronized (timestamps) {
                timestamps.removeIf(ts -> ts < threshold);
            }
        });
        clientRequests.values().removeIf(Deque::isEmpty);
    }
    
    public void shutdown() {
        cleaner.shutdown();
    }
}
```

```java
import com.sun.net.httpserver.*;
import java.io.*;

public class RateLimitedHandler implements HttpHandler {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            byte[] response = "Rate limit exceeded".getBytes();
            exchange.sendResponseHeaders(429, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
            return;
        }
        
        byte[] response = "OK".getBytes();
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
```

```java
import com.sun.net.httpserver.*;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(8080), 0);
        server.createContext("/", new RateLimitedHandler());
        server.start();
        System.out.println("Server running on port 8080");
    }
}
```

The `RateLimiter` tracks request timestamps per IP in a sliding 60-second window. When a client exceeds 10 requests/minute, it returns `false` and the handler responds with HTTP 429 (Too Many Requests). A background thread cleans up expired entries to prevent memory bloat.