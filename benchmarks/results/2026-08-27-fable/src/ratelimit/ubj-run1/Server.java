package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

/** Example wiring: any existing handler gains rate limiting by being wrapped. */
public final class Server {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final int HTTP_OK = 200;

    public static void main(String[] args) throws IOException {
        HttpHandler greeting = exchange -> respondOk(exchange, "Hello, participant_001!\n");
        SlidingWindowRateLimiter limiter =
                new SlidingWindowRateLimiter(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1));

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RateLimitingHandler(greeting, limiter, Clock.systemUTC()));
        server.start();
        System.out.println("Listening on http://localhost:" + PORT);
    }

    private static void respondOk(HttpExchange exchange, String text) throws IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(HTTP_OK, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
