package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;

/**
 * Decorates any {@link HttpHandler} with per-client rate limiting.
 *
 * Clients are identified by remote IP address. A client over its limit
 * receives 429 with a Retry-After header, and the delegate is never invoked.
 */
public final class RateLimitingHandler implements HttpHandler {

    static final int HTTP_TOO_MANY_REQUESTS = 429;
    static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final byte[] REJECTION_BODY =
            "Too many requests, please retry later.\n".getBytes(StandardCharsets.UTF_8);

    private final HttpHandler delegate;
    private final SlidingWindowRateLimiter limiter;
    private final Clock clock;

    public RateLimitingHandler(HttpHandler delegate, SlidingWindowRateLimiter limiter, Clock clock) {
        this.delegate = delegate;
        this.limiter = limiter;
        this.clock = clock;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        SlidingWindowRateLimiter.Decision decision =
                limiter.tryAcquire(clientIdFor(exchange), clock.instant());
        if (!decision.allowed()) {
            reject(exchange, decision.retryAfterSeconds());
            return;
        }
        delegate.handle(exchange);
    }

    // ubj: remote IP only — behind a reverse proxy all clients share the proxy's IP;
    // switch to X-Forwarded-For from the trusted proxy hop when one is introduced.
    private static String clientIdFor(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private static void reject(HttpExchange exchange, long retryAfterSeconds) throws IOException {
        exchange.getResponseHeaders().set(RETRY_AFTER_HEADER, Long.toString(retryAfterSeconds));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, REJECTION_BODY.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(REJECTION_BODY);
        }
    }
}
