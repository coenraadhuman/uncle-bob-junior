import java.util.concurrent.ConcurrentHashMap;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RateLimitedHttpHandler implements HttpHandler {
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000; // 1 minute
    private final ConcurrentHashMap<String, ClientLimiter> limiters = new ConcurrentHashMap<>();

    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        ClientLimiter limiter = limiters.computeIfAbsent(clientIp, k -> new ClientLimiter());

        if (limiter.tryConsume()) {
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

    private static class ClientLimiter {
        private final long[] timestamps = new long[MAX_REQUESTS];
        private int index = 0;
        private boolean filled = false;

        synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            if (!filled && index < MAX_REQUESTS) {
                timestamps[index++] = now;
                if (index == MAX_REQUESTS) filled = true;
                return true;
            }
            if (now - timestamps[index] > WINDOW_MS) {
                timestamps[index] = now;
                index = (index + 1) % MAX_REQUESTS;
                return true;
            }
            return false;
        }
    }
}
