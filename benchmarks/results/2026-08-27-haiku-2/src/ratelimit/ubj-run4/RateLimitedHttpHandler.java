import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class RateLimitedHttpHandler implements HttpHandler {
    private final RateLimiter rateLimiter;

    public RateLimitedHttpHandler() {
        this.rateLimiter = new RateLimiter();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

        if (!rateLimiter.allowRequest(clientIp)) {
            sendTooManyRequestsResponse(exchange);
            return;
        }

        handleRequest(exchange);
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        String response = "Success";
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    private void sendTooManyRequestsResponse(HttpExchange exchange) throws IOException {
        String response = "Too many requests. Max 5 per minute.";
        exchange.sendResponseHeaders(429, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
