package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public final class RateLimitingHttpHandler implements HttpHandler {

    private static final int TOO_MANY_REQUESTS = 429;
    private static final int NO_RESPONSE_BODY = -1;

    private final HttpHandler delegate;
    private final ClientRateLimiter rateLimiter;

    public RateLimitingHttpHandler(HttpHandler delegate, ClientRateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = clientIdOf(exchange);
        if (!rateLimiter.allowRequest(clientId)) {
            rejectWithTooManyRequests(exchange);
            return;
        }
        delegate.handle(exchange);
    }

    private String clientIdOf(HttpExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRemoteAddress();
        return remoteAddress.getAddress().getHostAddress();
    }

    private void rejectWithTooManyRequests(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(TOO_MANY_REQUESTS, NO_RESPONSE_BODY);
        exchange.close();
    }
}
