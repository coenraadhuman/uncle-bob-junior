// RateLimitedHttpHandler.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class RateLimitedHttpHandler implements HttpHandler {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    private final long retryAfterSeconds;

    public RateLimitedHttpHandler(HttpHandler delegate, RateLimiter rateLimiter, Duration windowDuration) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        this.retryAfterSeconds = windowDuration.getSeconds();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = resolveClientId(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String resolveClientId(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "Rate limit exceeded. Please try again later.".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Retry-After", String.valueOf(retryAfterSeconds));
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
