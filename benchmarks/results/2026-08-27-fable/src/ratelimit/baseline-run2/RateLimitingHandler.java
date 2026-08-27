import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Wraps any HttpHandler and rejects clients that exceed the rate limit.
 */
public final class RateLimitingHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final RateLimiter limiter;

    public RateLimitingHandler(HttpHandler delegate, RateLimiter limiter) {
        this.delegate = delegate;
        this.limiter = limiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientKey = clientKey(exchange);
        long retryAfterSeconds = limiter.tryAcquire(clientKey);

        if (retryAfterSeconds > 0) {
            byte[] body = "Too many requests. Please try again later.\n"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Retry-After",
                    Long.toString(retryAfterSeconds));
            exchange.getResponseHeaders().set("Content-Type",
                    "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(429, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            return;
        }

        delegate.handle(exchange);
    }

    private String clientKey(HttpExchange exchange) {
        // Behind a trusted reverse proxy, prefer the first entry of
        // X-Forwarded-For instead. Do NOT trust that header from the
        // open internet, as clients can forge it.
        InetSocketAddress remote = exchange.getRemoteAddress();
        return remote.getAddress().getHostAddress();
    }
}
