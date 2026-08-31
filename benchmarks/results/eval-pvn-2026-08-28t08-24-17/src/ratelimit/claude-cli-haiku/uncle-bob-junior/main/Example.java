public class Example {
    public static void main(String[] args) {
        RateLimiter rateLimiter = new RateLimiter(10, 60_000);
        
        RateLimitingHandler handler = new RateLimitingHandler(
            rateLimiter,
            (request, response) -> {
                response.setStatus(200);
                response.setBody("OK");
            }
        );
    }
}
