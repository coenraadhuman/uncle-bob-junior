import com.sun.net.httpserver.*;
import java.io.IOException;

public class RateLimitedHttpHandler implements HttpHandler {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = getClientIp(exchange);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            sendResponse(exchange, 429, "Too Many Requests: Rate limit exceeded (5 requests per minute)");
            return;
        }
        
        // Add rate limit headers for visibility
        exchange.getResponseHeaders().add("X-RateLimit-Limit", "5");
        exchange.getResponseHeaders().add("X-RateLimit-Remaining", 
            String.valueOf(rateLimiter.getRemainingRequests(clientIp)));
        
        // Process the actual request
        handleRequest(exchange);
    }
    
    private String getClientIp(HttpExchange exchange) {
        String ip = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            return ip.split(",")[0].trim();
        }
        return exchange.getInetSocketAddress().getAddress().getHostAddress();
    }
    
    private void handleRequest(HttpExchange exchange) throws IOException {
        String response = "Request successful";
        sendResponse(exchange, 200, response);
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] response = message.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
