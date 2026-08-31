import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

class RateLimitedHttpHandler implements HttpHandler {
    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    
    public RateLimitedHttpHandler(HttpHandler delegate, RateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = getClientIp(exchange);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            sendTooManyRequestsResponse(exchange);
            return;
        }
        
        delegate.handle(exchange);
    }
    
    private String getClientIp(HttpExchange exchange) {
        String xForwardedFor = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null) {
            return xForwardedFor.split(",")[0].trim();
        }
        return exchange.getRemoteAddress().getHostString();
    }
    
    private void sendTooManyRequestsResponse(HttpExchange exchange) throws IOException {
        String response = "Rate limit exceeded. Maximum 10 requests per minute allowed.";
        exchange.getResponseHeaders().set("Retry-After", "60");
        exchange.sendResponseHeaders(429, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
