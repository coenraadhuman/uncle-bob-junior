import com.sun.net.httpserver.*;
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
            sendTooManyRequests(exchange);
            return;
        }
        
        delegate.handle(exchange);
    }
    
    private void sendTooManyRequests(HttpExchange exchange) throws IOException {
        String response = "Too many requests";
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.getResponseHeaders().set("Retry-After", "60");
        exchange.sendResponseHeaders(429, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
