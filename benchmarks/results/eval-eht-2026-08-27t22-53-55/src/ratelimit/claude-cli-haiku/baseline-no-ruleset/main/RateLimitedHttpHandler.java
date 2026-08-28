import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class RateLimitedHttpHandler implements HttpHandler {
    private final RateLimiter rateLimiter;
    
    public RateLimitedHttpHandler(int requestsPerMinute) {
        this.rateLimiter = new RateLimiter(requestsPerMinute);
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            exchange.sendResponseHeaders(429, -1); // 429 Too Many Requests
            exchange.close();
            return;
        }
        
        // Handle request normally
        String response = "OK";
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new RateLimitedHttpHandler(5)); // 5 requests/minute
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Server listening on port 8000 with rate limiting (5 req/min)");
    }
}
