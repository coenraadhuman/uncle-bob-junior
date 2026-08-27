import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

/**
 * HTTP handler with rate limiting applied.
 */
public class RateLimitedHandler implements HttpHandler {
    private final RateLimiter rateLimiter;
    private final HttpHandler delegate;
    
    public RateLimitedHandler(HttpHandler delegate, RateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = getClientIp(exchange);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            // Return 429 Too Many Requests
            byte[] response = "Rate limit exceeded. Max 5 requests per minute.".getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.getResponseHeaders().set("Retry-After", "60");
            exchange.sendResponseHeaders(429, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
            return;
        }
        
        // Add rate limit info to response headers
        int remaining = rateLimiter.getRemainingRequests(clientIp);
        exchange.getResponseHeaders().set("X-RateLimit-Remaining", String.valueOf(remaining));
        exchange.getResponseHeaders().set("X-RateLimit-Limit", "5");
        
        // Delegate to the actual handler
        delegate.handle(exchange);
    }
    
    private String getClientIp(HttpExchange exchange) {
        // Try X-Forwarded-For header first (for proxies)
        String forwarded = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        
        // Fall back to direct connection IP
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}
