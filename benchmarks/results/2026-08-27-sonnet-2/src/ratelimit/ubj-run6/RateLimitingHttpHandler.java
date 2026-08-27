// RateLimitingHttpHandler.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int RATE_LIMIT_STATUS_CODE = 429;
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    private final Duration windowDuration;

    public RateLimitingHttpHandler(HttpHandler delegate, RateLimiter rateLimiter, Duration windowDuration) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        this.windowDuration = windowDuration;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdOf(exchange);
        if (!rateLimiter.tryAcquire(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdOf(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        byte[] body = "Too many requests, slow down.".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, String.valueOf(windowDuration.toSeconds()));
        exchange.sendResponseHeaders(RATE_LIMIT_STATUS_CODE, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
