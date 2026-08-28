import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter = new RateLimiter(5, 60_000);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                            Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setHeader("Retry-After", "60");
            response.getWriter().write("Rate limit exceeded");
            return false;
        }
        
        return true;
    }
}
