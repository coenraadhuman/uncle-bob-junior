import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            response.setHeader("Retry-After", "60");
            response.sendError(429, "Too Many Requests");
            return false;
        }
        
        return true;
    }
}
