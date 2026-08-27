import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class RateLimitedHttpHandler {
    private static final RateLimiter rateLimiter = new RateLimiter(
        10,                    // 10 requests
        60_000                 // per 60 seconds (1 minute)
    );
    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api", exchange -> {
            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
            
            if (!rateLimiter.allowRequest(clientIp)) {
                exchange.getResponseHeaders().set("X-RateLimit-Remaining", "0");
                exchange.getResponseHeaders().set("Retry-After", "60");
                exchange.sendResponseHeaders(429, -1); // Too Many Requests
                exchange.close();
                return;
            }
            
            int remaining = rateLimiter.getRemainingRequests(clientIp);
            exchange.getResponseHeaders().set("X-RateLimit-Remaining", String.valueOf(remaining));
            exchange.getResponseHeaders().set("X-RateLimit-Limit", "10");
            
            String response = "Hello, " + clientIp + "!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });
        
        server.start();
        System.out.println("Server started on port 8080");
    }
}
