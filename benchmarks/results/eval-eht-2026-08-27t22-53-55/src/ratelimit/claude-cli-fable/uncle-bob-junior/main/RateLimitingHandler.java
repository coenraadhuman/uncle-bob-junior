import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

/**
 * Wraps any HttpHandler and answers 429 Too Many Requests once a client
 * exceeds its per-window limit. Keys clients by remote IP address.
 */
public final class RateLimitingHandler implements HttpHandler {

    public static final int DEFAULT_MAX_REQUESTS_PER_MINUTE = 5;
    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);

    private final SlidingWindowRateLimiter rateLimiter;
    private final HttpHandler delegate;

    public RateLimitingHandler(SlidingWindowRateLimiter rateLimiter, HttpHandler delegate) {
        this.rateLimiter = rateLimiter;
        this.delegate = delegate;
    }

    public static RateLimitingHandler withDefaultLimit(HttpHandler delegate) {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(
                DEFAULT_MAX_REQUESTS_PER_MINUTE, ONE_MINUTE, Clock.systemUTC());
        return new RateLimitingHandler(limiter, delegate);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!rateLimiter.tryAcquire(clientIdOf(exchange))) {
            rejectTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    // ubj: remote IP is correct only when clients connect directly; behind a
    // trusted proxy, switch to the proxy-supplied client address header
    private static String clientIdOf(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "Too many requests, please retry later.".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Retry-After", String.valueOf(rateLimiter.windowSeconds()));
        exchange.sendResponseHeaders(STATUS_TOO_MANY_REQUESTS, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
