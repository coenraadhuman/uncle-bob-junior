// RateLimiterWiringExample.java
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

public final class RateLimiterWiringExample {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(MAX_REQUESTS_PER_MINUTE, RATE_LIMIT_WINDOW);
        HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
        server.createContext("/api", new RateLimitedHttpHandler(
                exchange -> exchange.sendResponseHeaders(200, -1),
                rateLimiter,
                RATE_LIMIT_WINDOW));
        server.start();
    }
}
