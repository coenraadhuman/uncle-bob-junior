import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RateLimitedHttpHandler implements HttpHandler {
    private final RateLimiter rateLimiter;
    private final HttpHandler delegate;

    public RateLimitedHttpHandler(HttpHandler delegate, RateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = extractClientId(exchange);

        if (!rateLimiter.allowRequest(clientId)) {
            sendRateLimitExceeded(exchange, clientId);
            return;
        }

        addRateLimitHeaders(exchange, clientId);
        delegate.handle(exchange);
    }

    private void sendRateLimitExceeded(HttpExchange exchange, String clientId) throws IOException {
        addRateLimitHeaders(exchange, clientId);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Retry-After", "60");

        String response = "{\"error\":\"Rate limit exceeded. Max 10 requests per minute.\"}";
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(429, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    private void addRateLimitHeaders(HttpExchange exchange, String clientId) {
        int remaining = rateLimiter.remainingRequests(clientId);
        exchange.getResponseHeaders().set("X-RateLimit-Remaining", String.valueOf(remaining));
        exchange.getResponseHeaders().set("X-RateLimit-Limit", "10");
        exchange.getResponseHeaders().set("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60_000));
    }

    private String extractClientId(HttpExchange exchange) {
        String xForwarded = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isEmpty()) {
            return xForwarded.split(",")[0].trim();
        }
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}
