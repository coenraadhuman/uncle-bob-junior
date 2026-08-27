// RateLimitingHandler.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.Duration;

public final class RateLimitingHandler implements HttpHandler {

    private static final int TOO_MANY_REQUESTS_STATUS = 429;
    private static final int NO_RESPONSE_BODY = -1;
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;
    private final Duration retryAfter;

    public RateLimitingHandler(HttpHandler delegate, RateLimiter rateLimiter, Duration retryAfter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
        this.retryAfter = retryAfter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdFrom(exchange);
        if (!rateLimiter.allowRequest(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdFrom(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add(RETRY_AFTER_HEADER, String.valueOf(retryAfter.toSeconds()));
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS_STATUS, NO_RESPONSE_BODY);
        exchange.close();
    }
}
