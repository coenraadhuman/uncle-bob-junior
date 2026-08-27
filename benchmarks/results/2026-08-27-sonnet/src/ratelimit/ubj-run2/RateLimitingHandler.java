import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Wraps a delegate handler, rejecting a client's requests once it exceeds the configured rate. */
public final class RateLimitingHandler implements HttpHandler {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Duration STALE_CLIENT_TTL = Duration.ofMinutes(10);
    private static final int TOO_MANY_REQUESTS = 429;
    private static final int NO_RESPONSE_BODY = -1;

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;

    public RateLimitingHandler(HttpHandler delegate) {
        this(delegate, new RateLimiter(MAX_REQUESTS_PER_MINUTE, WINDOW, Clock.systemUTC()));
    }

    RateLimitingHandler(HttpHandler delegate, RateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        startEvictionSchedule();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdOf(exchange);

        if (!rateLimiter.tryAcquire(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }

        delegate.handle(exchange);
    }

    private String clientIdOf(HttpExchange exchange) {
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(WINDOW.toSeconds()));
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS, NO_RESPONSE_BODY);
        exchange.close();
    }

    private void startEvictionSchedule() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "rate-limiter-eviction");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(
                () -> rateLimiter.evictStaleClients(STALE_CLIENT_TTL),
                STALE_CLIENT_TTL.toMinutes(), STALE_CLIENT_TTL.toMinutes(), TimeUnit.MINUTES);
    }
}
