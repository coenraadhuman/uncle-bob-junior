import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHandler implements HttpHandler {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getHostString();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            // Return 429 Too Many Requests
            exchange.getResponseHeaders().set("Retry-After", "60");
            exchange.sendResponseHeaders(429, 0);
            exchange.close();
            return;
        }
        
        // Handle the actual request
        String response = "Request accepted";
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
