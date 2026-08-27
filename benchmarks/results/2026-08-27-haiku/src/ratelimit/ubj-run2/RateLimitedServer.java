import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class RateLimitedServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        HttpHandler helloHandler = exchange -> {
            String response = "Hello, World!";
            byte[] bytes = response.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        };
        
        server.createContext("/", new RateLimitingHttpHandler(helloHandler, 10));
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server running on http://localhost:8080");
        System.out.println("Rate limit: 10 requests per minute per client");
    }
}
