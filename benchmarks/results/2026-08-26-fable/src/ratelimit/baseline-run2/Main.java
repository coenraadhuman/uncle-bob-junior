import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Your existing business handler, unchanged.
        com.sun.net.httpserver.HttpHandler apiHandler = exchange -> {
            byte[] body = "Hello!\n".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (var os = exchange.getResponseBody()) {
                os.write(body);
            }
        };

        // Allow 5 requests per client per rolling 60 seconds.
        server.createContext("/api",
                new RateLimitingHandler(apiHandler, 5, TimeUnit.MINUTES.toMillis(1)));

        server.start();
        System.out.println("Listening on http://localhost:8080/api");
    }
}
