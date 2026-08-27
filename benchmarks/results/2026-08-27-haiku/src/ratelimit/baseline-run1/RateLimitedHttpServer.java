import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.InetSocketAddress;

public class RateLimitedHttpServer {
    private static final RateLimiter rateLimiter = new RateLimiter(10, 60_000); // 10 requests per 60 seconds

    static class RateLimitedHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
            
            if (!rateLimiter.allowRequest(clientIp)) {
                // Rate limit exceeded
                exchange.getResponseHeaders().set("Retry-After", "60");
                exchange.sendResponseHeaders(429, 0); // 429 Too Many Requests
                exchange.close();
                return;
            }

            // Handle the actual request
            String response = "Hello, " + clientIp + "!";
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new RateLimitedHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server running on port 8080");
    }
}
