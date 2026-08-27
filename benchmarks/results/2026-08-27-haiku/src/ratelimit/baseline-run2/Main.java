import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Create your main handler
        HttpHandler myHandler = exchange -> {
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write("OK".getBytes());
            exchange.close();
        };

        // Wrap with rate limiting: 10 requests per minute per client
        server.createContext("/api", new RateLimitedHttpHandler(myHandler, 10));

        server.setExecutor(null);
        server.start();
        System.out.println("Server listening on port 8080");
    }
}
