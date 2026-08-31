I'll provide a rate limiting solution using a sliding window approach that tracks requests per client IP.

**Assumptions:**
- Using Java's built-in `HttpServer` (com.sun.net.httpserver)
- Rate limit: 5 requests per 60 seconds per client
- Track clients by IP address
- Return HTTP 429 when limit exceeded

**Solution:**

```java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitingHandler implements HttpHandler {
    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, LinkedList<Long>> clientRequests = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        long now = System.currentTimeMillis();

        LinkedList<Long> requests = clientRequests.computeIfAbsent(clientIp, k -> new LinkedList<>());

        synchronized (requests) {
            requests.removeIf(timestamp -> now - timestamp > windowMillis);

            if (requests.size() >= maxRequests) {
                exchange.getResponseHeaders().set("Retry-After", "60");
                exchange.sendResponseHeaders(429, 0);
                exchange.close();
                return;
            }

            requests.add(now);
        }

        delegate.handle(exchange);
    }
}
```

**Usage example:**

```java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/api", new RateLimitingHandler(
            exchange -> {
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().write("Hello World".getBytes());
                exchange.close();
            },
            5,           // max 5 requests
            60000        // per 60 seconds
        ));
        
        server.start();
        System.out.println("Server started on port 8080");
    }
}
```

The handler wraps any existing handler, tracks request timestamps per client IP in a sliding window, and rejects requests (429 status) once the limit is exceeded.