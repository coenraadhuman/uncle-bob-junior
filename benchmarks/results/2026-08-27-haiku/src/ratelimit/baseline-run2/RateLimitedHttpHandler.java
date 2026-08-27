import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHttpHandler implements HttpHandler {
    private final RateLimiter rateLimiter;
    private final HttpHandler delegate;

    public RateLimitedHttpHandler(HttpHandler delegate, int requestsPerMinute) {
        this.delegate = delegate;
        this.rateLimiter = new RateLimiter(requestsPerMinute);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = getClientId(exchange);

        if (!rateLimiter.allowRequest(clientId)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            exchange.sendResponseHeaders(429, 0);
            exchange.close();
            return;
        }

        delegate.handle(exchange);
    }

    private String getClientId(HttpExchange exchange) {
        // Try to get forwarded IP first (for proxied requests)
        String forwarded = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        // Fall back to remote address
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}
