import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final RateLimiter limiter;
    // Only trust X-Forwarded-For if a trusted reverse proxy sets it.
    private final boolean trustForwardedFor;

    public RateLimitingHandler(HttpHandler delegate, RateLimiter limiter, boolean trustForwardedFor) {
        this.delegate = delegate;
        this.limiter = limiter;
        this.trustForwardedFor = trustForwardedFor;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientId(exchange);
        if (limiter.tryAcquire(clientId)) {
            delegate.handle(exchange);
            return;
        }
        long retryAfter = limiter.secondsUntilReset(clientId);
        byte[] body = "Too many requests. Please try again later.\n"
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Retry-After", Long.toString(retryAfter));
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private String clientId(HttpExchange exchange) {
        if (trustForwardedFor) {
            String xff = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
        }
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    public static void main(String[] args) throws IOException {
        RateLimiter limiter = new RateLimiter(5, Duration.ofMinutes(1));

        HttpHandler myHandler = exchange -> {
            byte[] body = "Hello!\n".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        };

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new RateLimitingHandler(myHandler, limiter, false));
        server.setExecutor(Executors.newFixedThreadPool(8));

        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limiter-eviction");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(limiter::evictExpired, 1, 1, TimeUnit.MINUTES);

        server.start();
        System.out.println("Listening on http://localhost:8080 (5 requests/minute per client)");
    }
}
