import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.sun.net.httpserver.HttpServer;

public final class Main {

    public static void main(String[] args) throws Exception {
        // 5 requests per client per rolling 60-second window.
        RateLimiter limiter = new RateLimiter(5, Duration.ofMinutes(1));

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new RateLimitingHandler(exchange -> {
            byte[] body = "Hello\n".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        }, limiter));
        server.start();
        System.out.println("Listening on http://localhost:8080");
    }
}
