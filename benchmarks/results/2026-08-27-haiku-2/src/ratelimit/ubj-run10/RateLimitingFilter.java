import java.util.concurrent.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class RateLimitingFilter implements Filter {
  private static final ConcurrentHashMap<String, RateLimiter> LIMITERS = 
    new ConcurrentHashMap<>();
  private static final long CLEANUP_INTERVAL_MS = 300_000; // 5 minutes
  private static final long LIMITER_EXPIRY_MS = 600_000; // 10 minutes
  private long lastCleanupTime = System.currentTimeMillis();
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, 
      FilterChain chain) throws ServletException, java.io.IOException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    
    String clientIp = getClientIp(httpRequest);
    RateLimiter limiter = LIMITERS.computeIfAbsent(clientIp, k -> new RateLimiter());
    
    if (limiter.tryConsume()) {
      chain.doFilter(request, response);
    } else {
      httpResponse.setStatus(429); // Too Many Requests
      httpResponse.getWriter().write("Rate limit exceeded");
    }
    
    cleanupExpiredLimiters();
  }
  
  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isEmpty()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
  
  private void cleanupExpiredLimiters() {
    long now = System.currentTimeMillis();
    if (now - lastCleanupTime < CLEANUP_INTERVAL_MS) {
      return;
    }
    lastCleanupTime = now;
    
    LIMITERS.values().removeIf(limiter -> 
      now - limiter.getLastAccessTime() > LIMITER_EXPIRY_MS);
  }
  
  @Override
  public void init(FilterConfig config) {}
  
  @Override
  public void destroy() {}
}
