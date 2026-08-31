import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class RateLimitedServerExample {
    public static void main(String[] args) throws IOException {
        ClientRateLimiter rateLimiter = new ClientRateLimiter(10, 60_000);
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        HttpHandler baseHandler = exchange -> {
            String response = "Hello, world!";
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        };
        
        server.createContext("/", new RateLimitedHttpHandler(baseHandler, rateLimiter));
        server.start();
        System.out.println("Server started on port 8080 (10 requests/min per IP)");
    }
}
