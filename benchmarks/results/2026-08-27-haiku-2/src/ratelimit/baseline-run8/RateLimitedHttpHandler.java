import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class RateLimitedHttpHandler implements HttpHandler {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            // Return 429 Too Many Requests
            exchange.getResponseHeaders().set("Retry-After", "60");
            byte[] response = "Rate limit exceeded. Max 10 requests per minute.".getBytes();
            exchange.sendResponseHeaders(429, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
            return;
        }
        
        // Handle the actual request
        String responseBody = "Hello, World!";
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, responseBody.length());
        exchange.getResponseBody().write(responseBody.getBytes());
        exchange.close();
    }
    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new RateLimitedHttpHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server started on port 8080");
    }
}
