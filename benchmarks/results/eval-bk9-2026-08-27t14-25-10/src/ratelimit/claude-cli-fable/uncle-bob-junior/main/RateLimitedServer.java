package ratelimit;

import com.sun.net.httpserver.HttpExchange;
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

/** Example wiring: a small server whose /hello endpoint is rate limited per client IP. */
public final class RateLimitedServer {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);
    private static final int STATUS_OK = 200;

    private RateLimitedServer() {
    }

    public static void main(String[] args) throws IOException {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(
                MAX_REQUESTS_PER_MINUTE, RATE_LIMIT_WINDOW, Clock.systemUTC());
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/hello", new RateLimitingHandler(limiter, RateLimitedServer::sayHello));
        scheduleIdleClientPurge(limiter);
        server.start();
    }

    private static void scheduleIdleClientPurge(SlidingWindowRateLimiter limiter) {
        ScheduledExecutorService housekeeping = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "rate-limit-housekeeping");
            thread.setDaemon(true);
            return thread;
        });
        long periodSeconds = RATE_LIMIT_WINDOW.toSeconds();
        housekeeping.scheduleAtFixedRate(
                limiter::purgeIdleClients, periodSeconds, periodSeconds, TimeUnit.SECONDS);
    }

    private static void sayHello(HttpExchange exchange) throws IOException {
        byte[] body = "Hello!".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(STATUS_OK, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
