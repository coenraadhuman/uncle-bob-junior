import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
  private final RateLimiter rateLimiter = new RateLimiter();
  private static final int TOO_MANY_REQUESTS = 429;
  
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    
    String clientIp = getClientIp(request);
    
    if (!rateLimiter.allowRequest(clientIp)) {
      response.setStatus(TOO_MANY_REQUESTS);
      response.getWriter().write("Rate limit exceeded. Max 10 requests per minute.");
      return false;
    }
    
    return true;
  }
  
  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isEmpty()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
