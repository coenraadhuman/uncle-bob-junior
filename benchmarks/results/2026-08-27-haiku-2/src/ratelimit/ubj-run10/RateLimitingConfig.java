import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.HandlerInterceptor;

@Configuration
public class RateLimitingConfig implements WebMvcConfigurer {
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new RateLimitingInterceptor());
  }
}

public class RateLimitingInterceptor implements HandlerInterceptor {
  private static final ConcurrentHashMap<String, RateLimiter> LIMITERS = 
    new ConcurrentHashMap<>();
  
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
      Object handler) throws Exception {
    String clientIp = getClientIp(request);
    RateLimiter limiter = LIMITERS.computeIfAbsent(clientIp, k -> new RateLimiter());
    
    if (!limiter.tryConsume()) {
      response.setStatus(429);
      response.getWriter().write("Rate limit exceeded");
      return false;
    }
    return true;
  }
  
  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    return (forwarded != null && !forwarded.isEmpty()) 
      ? forwarded.split(",")[0].trim() 
      : request.getRemoteAddr();
  }
}
