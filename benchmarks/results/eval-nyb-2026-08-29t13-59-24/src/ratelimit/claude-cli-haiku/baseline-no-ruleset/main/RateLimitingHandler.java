import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitingHandler implements HttpHandler {
    private final HttpHandler delegate;
    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, LinkedList<Long>> clientRequests = new ConcurrentHashMap<>();

    public RateLimitingHandler(HttpHandler delegate, int maxRequests, long windowMillis) {
        this.delegate = delegate;
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        long now = System.currentTimeMillis();

        LinkedList<Long> requests = clientRequests.computeIfAbsent(clientIp, k -> new LinkedList<>());

        synchronized (requests) {
            requests.removeIf(timestamp -> now - timestamp > windowMillis);

            if (requests.size() >= maxRequests) {
                exchange.getResponseHeaders().set("Retry-After", "60");
                exchange.sendResponseHeaders(429, 0);
                exchange.close();
                return;
            }

            requests.add(now);
        }

        delegate.handle(exchange);
    }
}
