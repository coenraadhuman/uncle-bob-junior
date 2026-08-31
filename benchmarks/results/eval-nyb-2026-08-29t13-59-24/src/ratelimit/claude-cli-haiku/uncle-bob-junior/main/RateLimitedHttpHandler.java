import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHttpHandler implements HttpHandler {
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final String TOO_MANY_REQUESTS_MSG = "Too many requests. Maximum 5 requests per minute.";
    
    private final RateLimiter rateLimiter;
    private final HttpHandler delegate;
    
    public RateLimitedHttpHandler(HttpHandler delegate) {
        this.delegate = delegate;
        this.rateLimiter = new RateLimiter(MAX_REQUESTS_PER_MINUTE);
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            sendTooManyRequestsResponse(exchange);
            return;
        }
        
        delegate.handle(exchange);
    }
    
    private void sendTooManyRequestsResponse(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.getResponseHeaders().set("Retry-After", "60");
        exchange.sendResponseHeaders(429, TOO_MANY_REQUESTS_MSG.length());
        exchange.getResponseBody().write(TOO_MANY_REQUESTS_MSG.getBytes());
        exchange.close();
    }
}
