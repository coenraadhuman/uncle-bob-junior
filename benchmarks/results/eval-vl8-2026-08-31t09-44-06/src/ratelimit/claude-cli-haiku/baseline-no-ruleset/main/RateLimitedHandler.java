import com.sun.net.httpserver.*;
import java.io.*;

public class RateLimitedHandler implements HttpHandler {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            byte[] response = "Rate limit exceeded".getBytes();
            exchange.sendResponseHeaders(429, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
            return;
        }
        
        byte[] response = "OK".getBytes();
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
