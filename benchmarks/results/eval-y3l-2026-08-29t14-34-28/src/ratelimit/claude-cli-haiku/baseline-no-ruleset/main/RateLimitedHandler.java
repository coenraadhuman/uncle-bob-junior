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
