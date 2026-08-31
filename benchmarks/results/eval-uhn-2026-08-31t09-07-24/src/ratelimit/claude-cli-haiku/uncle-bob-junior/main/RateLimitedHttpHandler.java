import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHttpHandler implements HttpHandler {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = getClientIp(exchange);
        
        if (!rateLimiter.isAllowed(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            sendResponse(exchange, 429, "Too Many Requests");
            return;
        }
        
        handleRequest(exchange);
    }
    
    private String getClientIp(HttpExchange exchange) {
        String forwarded = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0] : exchange.getRemoteAddress().getHostName();
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        exchange.sendResponseHeaders(statusCode, message.length());
        exchange.getResponseBody().write(message.getBytes());
        exchange.close();
    }
    
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String message = "Request processed";
        exchange.sendResponseHeaders(200, message.length());
        exchange.getResponseBody().write(message.getBytes());
        exchange.close();
    }
}
