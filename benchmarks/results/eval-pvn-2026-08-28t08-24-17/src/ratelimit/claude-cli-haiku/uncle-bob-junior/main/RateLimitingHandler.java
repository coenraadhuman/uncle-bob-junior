public class RateLimitingHandler {
    private static final int RATE_LIMIT_STATUS = 429;
    private static final String RATE_LIMIT_MESSAGE = "Too many requests";
    
    private final RateLimiter rateLimiter;
    private final RequestHandler nextHandler;
    
    public RateLimitingHandler(RateLimiter rateLimiter, RequestHandler nextHandler) {
        this.rateLimiter = rateLimiter;
        this.nextHandler = nextHandler;
    }
    
    public void handle(HttpRequest request, HttpResponse response) {
        String clientIp = request.getRemoteAddr();
        
        if (!rateLimiter.isAllowed(clientIp)) {
            response.setStatus(RATE_LIMIT_STATUS);
            response.setBody(RATE_LIMIT_MESSAGE);
            return;
        }
        
        nextHandler.handle(request, response);
    }
    
    @FunctionalInterface
    public interface RequestHandler {
        void handle(HttpRequest request, HttpResponse response);
    }
}
