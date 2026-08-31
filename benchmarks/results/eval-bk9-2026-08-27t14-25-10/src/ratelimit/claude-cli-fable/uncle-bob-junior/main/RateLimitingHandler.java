package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Wraps another handler and rejects clients that exceed the rate limit with
 * 429 Too Many Requests and a Retry-After header (in seconds).
 * Clients are identified by remote IP address, so deploy behind a proxy only
 * if the proxy preserves the real client address.
 */
public final class RateLimitingHandler implements HttpHandler {

    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final long MILLIS_PER_SECOND = 1_000;
    private static final byte[] REJECTION_BODY =
            "Too many requests, please retry later.".getBytes(StandardCharsets.UTF_8);

    private final SlidingWindowRateLimiter rateLimiter;
    private final HttpHandler delegate;

    public RateLimitingHandler(SlidingWindowRateLimiter rateLimiter, HttpHandler delegate) {
        this.rateLimiter = rateLimiter;
        this.delegate = delegate;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        RateLimitDecision decision = rateLimiter.check(clientIdOf(exchange));
        if (!decision.allowed()) {
            reject(exchange, decision);
            return;
        }
        delegate.handle(exchange);
    }

    private static String clientIdOf(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private static void reject(HttpExchange exchange, RateLimitDecision decision) throws IOException {
        exchange.getResponseHeaders().set(RETRY_AFTER_HEADER, Long.toString(retryAfterSeconds(decision)));
        exchange.sendResponseHeaders(STATUS_TOO_MANY_REQUESTS, REJECTION_BODY.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(REJECTION_BODY);
        }
    }

    private static long retryAfterSeconds(RateLimitDecision decision) {
        // Round up so clients never retry a moment before a slot actually frees.
        long millis = decision.retryAfter().toMillis();
        return Math.max(1, (millis + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND);
    }
}
