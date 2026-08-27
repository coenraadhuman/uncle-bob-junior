// Server.java — example wiring
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;

public final class Server {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        RateLimiter rateLimiter =
                new RateLimiter(MAX_REQUESTS_PER_MINUTE, RATE_LIMIT_WINDOW, Clock.systemUTC());

        server.createContext("/", new RateLimitingHttpHandler(
                exchange -> {
                    byte[] response = "OK".getBytes();
                    exchange.sendResponseHeaders(200, response.length);
                    exchange.getResponseBody().write(response);
                    exchange.close();
                },
                rateLimiter));

        server.start();
    }
}
