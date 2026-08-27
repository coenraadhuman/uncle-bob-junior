import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitingHandler implements HttpHandler {
    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    
    public RateLimitingHandler(HttpHandler delegate) {
        this.delegate = delegate;
        this.rateLimiter = new RateLimiter();
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = getClientIp(exchange);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            sendTooManyRequests(exchange);
            return;
        }
        
        delegate.handle(exchange);
    }
    
    private String getClientIp(HttpExchange exchange) {
        String forwardedFor = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return exchange.getInetSocketAddress().getAddress().getHostAddress();
    }
    
    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        String response = "Too Many Requests";
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.getResponseHeaders().set("Retry-After", "60");
        exchange.sendResponseHeaders(429, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
