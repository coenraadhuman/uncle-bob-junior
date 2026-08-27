import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHttpHandler implements HttpHandler {
    private final RateLimiter rateLimiter = new RateLimiter(10);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            exchange.sendResponseHeaders(429, 0);
            exchange.getResponseBody().write("{\"error\": \"Rate limit exceeded\"}".getBytes());
            exchange.close();
            return;
        }
        
        // Handle normal request
        exchange.sendResponseHeaders(200, 0);
        exchange.getResponseBody().write("OK".getBytes());
        exchange.close();
    }
}
