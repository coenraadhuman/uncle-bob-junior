import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Decorator that rejects requests over the per-client limit with 429,
 * delegating allowed requests to the wrapped handler.
 */
public final class RateLimitingHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    private final HttpHandler delegate;
    private final SlidingWindowRateLimiter rateLimiter;

    public RateLimitingHandler(HttpHandler delegate, SlidingWindowRateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdOf(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectTooManyRequests(exchange, clientId);
            return;
        }
        delegate.handle(exchange);
    }

    // ubj: keys on the socket address; behind a proxy or load balancer,
    // switch to a trusted X-Forwarded-For header or all clients share one key.
    private String clientIdOf(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectTooManyRequests(HttpExchange exchange, String clientId) throws IOException {
        byte[] body = "Too many requests. Please retry later.\n"
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders()
                .set("Retry-After", Long.toString(rateLimiter.secondsUntilRetry(clientId)));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
