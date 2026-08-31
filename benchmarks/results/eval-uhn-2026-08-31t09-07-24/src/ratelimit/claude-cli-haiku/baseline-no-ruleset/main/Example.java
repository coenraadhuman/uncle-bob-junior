import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.InetSocketAddress;

// Example usage
public class Example {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 5 requests per 60 seconds (1 minute)
        HttpHandler rateLimited = new RateLimitedHttpHandler(
            exchange -> {
                exchange.sendResponseHeaders(200, 5);
                exchange.getResponseBody().write("OK".getBytes());
                exchange.getResponseBody().close();
            },
            5,
            60_000
        );

        server.createContext("/api", rateLimited);
        server.start();
        System.out.println("Server started on port 8080");
    }
}
