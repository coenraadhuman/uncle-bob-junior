public class RateLimitingHandler {
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private final ConcurrentHashMap<String, TokenBucketRateLimiter> limiters;

    public RateLimitingHandler() {
        this.limiters = new ConcurrentHashMap<>();
    }

    public boolean isAllowed(String clientId) {
        TokenBucketRateLimiter limiter = limiters.computeIfAbsent(
            clientId,
            key -> new TokenBucketRateLimiter()
        );
        return limiter.allowRequest();
    }

    public void handleHttpRequest(HttpExchange exchange) throws IOException {
        String clientId = exchange.getRemoteAddress().getAddress().getHostAddress();
        if (!isAllowed(clientId)) {
            exchange.sendResponseHeaders(429, -1);  // Too Many Requests
            return;
        }
        processRequest(exchange);
    }

    private void processRequest(HttpExchange exchange) throws IOException {
        String response = "OK";
        exchange.sendResponseHeaders(200, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
