import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class RateLimitedHandler implements HttpHandler {
    private final HttpHandler delegate;
    private final RateLimiter rateLimiter;

    public RateLimitedHandler(HttpHandler delegate, RateLimiter rateLimiter) {
        this.delegate = delegate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = rateLimiter.getClientId(exchange);

        if (!rateLimiter.allowRequest(clientId)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            byte[] response = "429 Too Many Requests\n".getBytes();
            exchange.sendResponseHeaders(429, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
            return;
        }

        delegate.handle(exchange);
    }
}
