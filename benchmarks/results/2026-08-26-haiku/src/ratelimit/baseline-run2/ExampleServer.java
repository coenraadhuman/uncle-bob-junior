import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

// Example usage:
public class ExampleServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        RateLimiter rateLimiter = new RateLimiter();
        
        // Wrap your handler with rate limiting
        HttpHandler originalHandler = exchange -> {
            exchange.sendResponseHeaders(200, 5);
            exchange.getResponseBody().write("Hello".getBytes());
            exchange.close();
        };
        
        server.createContext("/", new RateLimitedHandler(originalHandler, rateLimiter));
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
    }
}
