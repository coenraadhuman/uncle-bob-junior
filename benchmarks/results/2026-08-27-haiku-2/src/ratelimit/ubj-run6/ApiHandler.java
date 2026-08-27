public class ApiHandler {
    private final RateLimiter rateLimiter = new RateLimiter(10);

    public void handleRequest(String clientIp, HttpRequest request, HttpResponse response) {
        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(429);
            response.setBody("{\"error\":\"Too many requests\"}");
            return;
        }
        // Process request normally
    }
}
