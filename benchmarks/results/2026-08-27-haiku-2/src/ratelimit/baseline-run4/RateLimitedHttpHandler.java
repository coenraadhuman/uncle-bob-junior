import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class RateLimitedHttpHandler {
    private final RateLimiter rateLimiter;

    public RateLimitedHttpHandler() {
        // 10 requests per minute (60,000 milliseconds)
        this.rateLimiter = new RateLimiter(10, TimeUnit.MINUTES.toMillis(1));
    }

    public HttpHandler createHandler(HttpHandler delegate) {
        return exchange -> {
            String clientIp = getClientIp(exchange);
            
            if (!rateLimiter.allowRequest(clientIp)) {
                sendRateLimitResponse(exchange);
                return;
            }
            
            delegate.handle(exchange);
        };
    }

    private String getClientIp(HttpExchange exchange) {
        String forwardedFor = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return exchange.getInetAddress().getHostAddress();
    }

    private void sendRateLimitResponse(HttpExchange exchange) throws IOException {
        String response = "Rate limit exceeded. Maximum 10 requests per minute.";
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.getResponseHeaders().set("Retry-After", "60");
        exchange.sendResponseHeaders(429, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    public static void main(String[] args) throws IOException {
        RateLimitedHttpHandler rateLimitHandler = new RateLimitedHttpHandler();
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Wrap your actual handler with rate limiting
        HttpHandler yourHandler = exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 7);
            exchange.getResponseBody().write("Success".getBytes());
            exchange.close();
        };
        
        server.createContext("/api", rateLimitHandler.createHandler(yourHandler));
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server running on http://localhost:8080");
    }
}
