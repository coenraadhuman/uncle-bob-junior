import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Example {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        RateLimiter rateLimiter = new RateLimiter(5); // 5 requests per minute

        HttpHandler originalHandler = new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                byte[] response = "Hello, World!\n".getBytes();
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            }
        };

        server.createContext("/", new RateLimitedHandler(originalHandler, rateLimiter));
        server.setExecutor(null);
        server.start();
        System.out.println("Server running on http://localhost:8080");
    }
}
