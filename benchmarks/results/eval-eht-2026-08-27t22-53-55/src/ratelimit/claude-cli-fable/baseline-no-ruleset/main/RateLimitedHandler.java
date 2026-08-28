import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Wraps any HttpHandler with per-client rate limiting.
 * Over-limit requests get 429 Too Many Requests with a Retry-After header.
 */
public final class RateLimitedHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final RateLimiter limiter;

    public RateLimitedHandler(HttpHandler delegate, RateLimiter limiter) {
        this.delegate = delegate;
        this.limiter = limiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientId(exchange);

        if (limiter.tryAcquire(clientId)) {
            delegate.handle(exchange);
            return;
        }

        byte[] body = "Too many requests. Please try again later.\n"
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Retry-After",
                String.valueOf(limiter.retryAfterSeconds(clientId)));
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(429, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private static String clientId(HttpExchange exchange) {
        // If you sit behind a trusted reverse proxy, prefer the first entry of
        // X-Forwarded-For here instead. Do not trust that header from the open
        // internet, since clients can spoof it to dodge the limit.
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}
