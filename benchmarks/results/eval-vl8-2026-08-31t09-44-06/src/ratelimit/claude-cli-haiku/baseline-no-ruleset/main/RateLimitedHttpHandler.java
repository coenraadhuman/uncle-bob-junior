import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class RateLimitedHttpHandler {
    private final RateLimiter rateLimiter;

    public RateLimitedHttpHandler(int requestsPerMinute) {
        this.rateLimiter = new RateLimiter(requestsPerMinute);
    }

    public HttpHandler createHandler(HttpHandler delegate) {
        return exchange -> {
            String clientIp = getClientIp(exchange);
            if (!rateLimiter.allowRequest(clientIp)) {
                exchange.getResponseHeaders().set("Retry-After", "60");
                exchange.sendResponseHeaders(429, 0);
                exchange.close();
                return;
            }
            delegate.handle(exchange);
        };
    }

    private String getClientIp(HttpExchange exchange) {
        String ip = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            return ip.split(",")[0].trim();
        }
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        RateLimitedHttpHandler rateLimiter = new RateLimitedHttpHandler(10); // 10 req/min per client

        server.createContext("/api", rateLimiter.createHandler(exchange -> {
            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        }));

        server.setExecutor(null);
        server.start();
        System.out.println("Server running on http://localhost:8080");
    }
}
