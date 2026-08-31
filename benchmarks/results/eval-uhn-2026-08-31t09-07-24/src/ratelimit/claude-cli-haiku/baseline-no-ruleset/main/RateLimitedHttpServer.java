import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class RateLimitedHttpServer {
    public static void main(String[] args) throws IOException {
        RateLimiter rateLimiter = new RateLimiter();
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/api", exchange -> {
            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
            
            if (!rateLimiter.allowRequest(clientIp)) {
                exchange.getResponseHeaders().set("Retry-After", "60");
                exchange.sendResponseHeaders(429, -1);
                exchange.close();
                return;
            }
            
            // Handle the request
            String response = "Hello from " + exchange.getRequestURI().getPath();
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server running on port 8080");
    }
}
