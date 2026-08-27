import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHttpHandler implements HttpHandler {
    private final TokenBucketRateLimiter rateLimiter;

    public RateLimitedHttpHandler() {
        this.rateLimiter = new TokenBucketRateLimiter(10);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.isAllowed(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            String response = "Rate limit exceeded. Max 10 requests per minute.";
            exchange.sendResponseHeaders(429, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
            return;
        }
        
        // Process the request normally
        String responseBody = "Request processed successfully";
        exchange.sendResponseHeaders(200, responseBody.getBytes().length);
        exchange.getResponseBody().write(responseBody.getBytes());
        exchange.close();
    }
}
