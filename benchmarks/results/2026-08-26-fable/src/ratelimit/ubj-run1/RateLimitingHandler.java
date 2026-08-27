import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Wraps any HttpHandler and rejects over-limit clients with 429. */
public final class RateLimitingHandler implements HttpHandler {

    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private static final String REJECTION_BODY = "Too many requests. Please retry later.\n";

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
            rejectAsRateLimited(exchange, clientId);
            return;
        }
        delegate.handle(exchange);
    }

    // ubj: identifies clients by socket IP; behind a trusted reverse proxy,
    // replace with the proxy-set forwarding header instead.
    private String clientIdOf(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectAsRateLimited(HttpExchange exchange, String clientId) throws IOException {
        byte[] body = REJECTION_BODY.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Retry-After",
                String.valueOf(rateLimiter.secondsUntilNextSlot(clientId)));
        exchange.sendResponseHeaders(STATUS_TOO_MANY_REQUESTS, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
