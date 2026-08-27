import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;

public class Example {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Wrap your handler with rate limiting
        server.createContext("/api", new RateLimitingHandler(exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 2);
            OutputStream os = exchange.getResponseBody();
            os.write("OK".getBytes());
            os.close();
        }));
        
        server.setExecutor(null);
        server.start();
        System.out.println("Server running on port 8080");
    }
}
