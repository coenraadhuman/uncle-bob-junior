import javax.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    private static final RateLimiter rateLimiter = new RateLimiter();
    private static final String RATE_LIMIT_EXCEEDED = "Too many requests";
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
            HttpServletResponse response, Object handler) throws Exception {
        String clientId = extractClientId(request);
        
        if (!rateLimiter.isAllowed(clientId)) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"" + RATE_LIMIT_EXCEEDED + "\"}");
            return false;
        }
        return true;
    }
    
    private String extractClientId(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
