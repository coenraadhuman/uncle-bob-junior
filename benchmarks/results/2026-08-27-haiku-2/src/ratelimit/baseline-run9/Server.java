import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

// Example usage:
public class Server {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Wrap your handler with rate limiting
        server.createContext("/api", new RateLimitingHandler(exchange -> {
            String response = "Success";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        }));
        
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
    }
}
