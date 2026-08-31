// RateLimitInterceptor.java
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter;
    
    public RateLimitInterceptor(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String clientIp = extractClientIp(request);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(429);
            response.getWriter().write("Too many requests");
            return false;
        }
        
        return true;
    }
    
    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
