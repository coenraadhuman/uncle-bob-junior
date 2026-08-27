import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class RateLimitingHandler implements HttpHandler {
    private final RateLimiter rateLimiter = new RateLimiter();
    private final HttpHandler delegate;
    
    public RateLimitingHandler(HttpHandler delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            exchange.sendResponseHeaders(429, -1);
            exchange.close();
            return;
        }
        
        delegate.handle(exchange);
    }
}
