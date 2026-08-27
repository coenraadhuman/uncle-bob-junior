import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

/** Wires the rate limiter around an example handler. */
public final class RateLimitedServer {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(
                MAX_REQUESTS_PER_WINDOW, WINDOW, Clock.systemUTC());
        server.createContext("/", new RateLimitingHandler(RateLimitedServer::sayHello, rateLimiter));
        server.start();
    }

    private static void sayHello(HttpExchange exchange) throws IOException {
        byte[] body = "Hello!\n".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
