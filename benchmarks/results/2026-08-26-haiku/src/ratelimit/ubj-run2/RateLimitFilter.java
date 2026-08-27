import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitFilter implements Filter {
  private final RateLimiter rateLimiter = new RateLimiter();
  private static final int TOO_MANY_REQUESTS = 429;
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    
    String clientIp = getClientIp(httpRequest);
    
    if (rateLimiter.allowRequest(clientIp)) {
      chain.doFilter(request, response);
    } else {
      httpResponse.setStatus(TOO_MANY_REQUESTS);
      httpResponse.getWriter().write("Rate limit exceeded. Max 10 requests per minute.");
    }
  }
  
  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isEmpty()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
  
  @Override
  public void init(FilterConfig config) {}
  
  @Override
  public void destroy() {}
}
