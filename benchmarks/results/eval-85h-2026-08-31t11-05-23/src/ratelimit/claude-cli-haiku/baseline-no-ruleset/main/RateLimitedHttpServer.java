import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;

public class RateLimitedHttpServer {
    public static void main(String[] args) throws IOException {
        RateLimiter rateLimiter = new RateLimiter(5); // 5 requests per minute
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/api", exchange -> {
            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
            
            if (!rateLimiter.allowRequest(clientIp)) {
                exchange.getResponseHeaders().set("Retry-After", "12");
                exchange.sendResponseHeaders(429, 0);
                exchange.getResponseBody().close();
                return;
            }
            
            String response = "Success";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        });
        
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server running on port 8080");
        
        // Cleanup task
        ScheduledExecutorService cleanup = Executors.newScheduledThreadPool(1);
        cleanup.scheduleAtFixedRate(rateLimiter::cleanup, 5, 5, TimeUnit.MINUTES);
    }
}
