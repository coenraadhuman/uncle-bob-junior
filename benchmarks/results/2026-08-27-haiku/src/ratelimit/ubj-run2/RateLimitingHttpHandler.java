import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public class RateLimitingHttpHandler implements HttpHandler {
    private static final int DEFAULT_REQUESTS_PER_MINUTE = 10;
    private static final int TOO_MANY_REQUESTS_STATUS = 429;
    private static final String RETRY_AFTER_SECONDS = "60";
    
    private final RateLimiter rateLimiter;
    private final HttpHandler nextHandler;
    
    public RateLimitingHttpHandler(HttpHandler nextHandler) {
        this(nextHandler, DEFAULT_REQUESTS_PER_MINUTE);
    }
    
    public RateLimitingHttpHandler(HttpHandler nextHandler, int requestsPerMinute) {
        this.nextHandler = nextHandler;
        this.rateLimiter = new RateLimiter(requestsPerMinute);
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = getClientIp(exchange);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            sendTooManyRequestsResponse(exchange);
            return;
        }
        
        nextHandler.handle(exchange);
    }
    
    private String getClientIp(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
    
    private void sendTooManyRequestsResponse(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.getResponseHeaders().set("Retry-After", RETRY_AFTER_SECONDS);
        
        String message = "Rate limit exceeded. Maximum " + DEFAULT_REQUESTS_PER_MINUTE + 
                         " requests per minute.";
        byte[] responseBytes = message.getBytes();
        
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS_STATUS, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
