import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHandler implements HttpHandler {
    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    
    public RateLimitedHandler(HttpHandler delegate) {
        this.delegate = delegate;
        this.rateLimiter = new RateLimiter(10); // 10 requests per minute
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            exchange.getResponseHeaders().add("Retry-After", "60");
            exchange.sendResponseHeaders(429, 0);
            exchange.getResponseBody().write("Rate limit exceeded".getBytes());
            exchange.close();
            return;
        }
        
        delegate.handle(exchange);
    }
}
