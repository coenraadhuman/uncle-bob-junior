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
