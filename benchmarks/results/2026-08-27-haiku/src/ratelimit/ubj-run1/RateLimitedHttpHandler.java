import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class RateLimitedHttpHandler implements HttpHandler {
    private final ClientRateLimiter rateLimiter;
    private final HttpHandler delegate;
    
    public RateLimitedHttpHandler(HttpHandler delegate, ClientRateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            byte[] response = "Rate limit exceeded: max 20 requests per minute.".getBytes();
            exchange.sendResponseHeaders(429, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
            return;
        }
        
        delegate.handle(exchange);
    }
}
