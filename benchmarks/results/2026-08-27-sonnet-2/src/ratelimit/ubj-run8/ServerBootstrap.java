// Example wiring
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;

public final class ServerBootstrap {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);

    public static HttpServer create(int port, com.sun.net.httpserver.HttpHandler apiHandler) throws java.io.IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        ClientRateLimiter rateLimiter =
                new ClientRateLimiter(MAX_REQUESTS_PER_MINUTE, RATE_LIMIT_WINDOW, Clock.systemUTC());
        server.createContext("/api", new RateLimitingHttpHandler(apiHandler, rateLimiter, RATE_LIMIT_WINDOW));
        return server;
    }
}
