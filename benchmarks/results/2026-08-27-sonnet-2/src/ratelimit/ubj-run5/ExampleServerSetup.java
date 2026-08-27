// ExampleServerSetup.java
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

final class ExampleServerSetup {
    private ExampleServerSetup() {
    }

    static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        RateLimiter rateLimiter = TokenBucketRateLimiter.withDefaultLimit();
        server.createContext("/api", new RateLimitingHandler(
                exchange -> exchange.sendResponseHeaders(200, NO_BODY_LENGTH),
                rateLimiter,
                Duration.ofSeconds(RETRY_AFTER_SECONDS)));
        server.start();
    }

    private static final int NO_BODY_LENGTH = -1;
    private static final int RETRY_AFTER_SECONDS = 30;
}
