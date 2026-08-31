import io.github.bucket4j.*;
import java.util.concurrent.ConcurrentHashMap;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RateLimitedHttpHandler implements HttpHandler {
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Bandwidth limit = Bandwidth.simple(10, java.time.Duration.ofMinutes(1));

    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> Bucket4j.builder()
                .addSimpleState("requests", limit)
                .build());

        if (bucket.tryConsume(1)) {
            handleRequest(exchange);
        } else {
            exchange.getResponseHeaders().set("Retry-After", "60");
            exchange.sendResponseHeaders(429, -1);
        }
    }

    private void handleRequest(HttpExchange exchange) throws java.io.IOException {
        String response = "OK";
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
