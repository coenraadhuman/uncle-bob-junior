public class RateLimitedHttpHandler implements HttpHandler {
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final String RETRY_AFTER_SECONDS = "60";
    
    private final HttpHandler delegate;
    private final ClientRateLimiter rateLimiter;
    
    public RateLimitedHttpHandler(HttpHandler delegate, long requestsPerMinute) {
        this.delegate = delegate;
        this.rateLimiter = new ClientRateLimiter(requestsPerMinute);
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String clientId = extractClientId(exchange);
        
        if (!rateLimiter.allowRequest(clientId)) {
            rejectTooManyRequests(exchange);
            return;
        }
        
        delegate.handle(exchange);
    }
    
    private String extractClientId(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
    
    private void rejectTooManyRequests(HttpExchange exchange) throws IOException {
        String response = "Too many requests";
        exchange.getResponseHeaders().set("Retry-After", RETRY_AFTER_SECONDS);
        exchange.sendResponseHeaders(HTTP_TOO_MANY_REQUESTS, response.length());
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }
}
