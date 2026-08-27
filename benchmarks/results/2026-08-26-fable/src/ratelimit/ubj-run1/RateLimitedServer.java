import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Example wiring: a plain handler protected by the rate limiter. */
public final class RateLimitedServer {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_WINDOW = Duration.ofMinutes(1);

    public static void main(String[] args) throws IOException {
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(
                MAX_REQUESTS_PER_MINUTE, RATE_WINDOW, Clock.systemUTC());

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RateLimitingHandler(RateLimitedServer::handleHello, rateLimiter));
        startIdleClientSweeper(rateLimiter);
        server.start();
    }

    private static void handleHello(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        byte[] body = "Hello!\n".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private static void startIdleClientSweeper(SlidingWindowRateLimiter rateLimiter) {
        ScheduledExecutorService sweeper = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "rate-limiter-sweeper");
            thread.setDaemon(true);
            return thread;
        });
        sweeper.scheduleAtFixedRate(rateLimiter::removeIdleClients,
                RATE_WINDOW.toSeconds(), RATE_WINDOW.toSeconds(), TimeUnit.SECONDS);
    }
}
