// RateLimitingInterceptor.java
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    private static final String RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
    private static final int TOO_MANY_REQUESTS = 429;
    
    private final ClientRateLimiter rateLimiter;

    public RateLimitingInterceptor(ClientRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws IOException {
        String clientIp = extractClientIp(request);
        
        if (!rateLimiter.isAllowed(clientIp)) {
            response.setStatus(TOO_MANY_REQUESTS);
            response.getWriter().write("Rate limit exceeded");
            return false;
        }
        
        response.addHeader(RATE_LIMIT_REMAINING, String.valueOf(rateLimiter.remaining(clientIp)));
        return true;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
