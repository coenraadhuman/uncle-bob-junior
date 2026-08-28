import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;

/** Example wiring: 5 requests per client per minute. */
public final class Main {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        RateLimiter limiter = new RateLimiter(5, Duration.ofMinutes(1));

        server.createContext("/api", new RateLimitedHandler(exchange -> {
            byte[] body = "Hello!\n".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        }, limiter));

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("Listening on http://localhost:8080/api");
    }
}
