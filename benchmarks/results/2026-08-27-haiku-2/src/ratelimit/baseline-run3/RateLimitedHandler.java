public class RateLimitedHandler implements HttpHandler {
    private final RateLimiter rateLimiter;

    public RateLimitedHandler() {
        // 5 requests per minute (60,000 ms)
        this.rateLimiter = new RateLimiter(5, 60_000);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();

        if (!rateLimiter.allowRequest(clientIp)) {
            exchange.getResponseHeaders().set("Retry-After", "60");
            exchange.sendResponseHeaders(429, 0);
            exchange.getResponseBody().close();
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, 0);
        exchange.getResponseBody().write("{\"status\":\"ok\"}".getBytes());
        exchange.close();
    }
}
