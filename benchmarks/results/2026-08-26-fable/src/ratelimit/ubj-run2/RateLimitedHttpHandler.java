import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

/** Wraps any HttpHandler and rejects clients that exceed the per-minute request limit. */
public final class RateLimitedHttpHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_WINDOW = Duration.ofMinutes(1);
    private static final byte[] REJECTION_BODY =
            "Too many requests. Please retry later.\n".getBytes(StandardCharsets.UTF_8);

    private final HttpHandler delegate;
    private final SlidingWindowRateLimiter rateLimiter;

    public RateLimitedHttpHandler(HttpHandler delegate) {
        this(delegate, new SlidingWindowRateLimiter(
                MAX_REQUESTS_PER_MINUTE, RATE_WINDOW, Clock.systemUTC()));
    }

    RateLimitedHttpHandler(HttpHandler delegate, SlidingWindowRateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientId(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectTooManyRequests(exchange, rateLimiter.retryAfterSeconds(clientId));
            return;
        }
        delegate.handle(exchange);
    }

    private static String clientId(HttpExchange exchange) {
        // Trusts the socket address; behind a reverse proxy, derive this
        // from a validated X-Forwarded-For header from the trusted proxy instead.
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private static void rejectTooManyRequests(HttpExchange exchange, long retryAfterSeconds)
            throws IOException {
        exchange.getResponseHeaders().set("Retry-After", Long.toString(retryAfterSeconds));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, REJECTION_BODY.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(REJECTION_BODY);
        }
    }
}
