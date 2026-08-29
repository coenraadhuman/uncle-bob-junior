import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

public class RateLimitingFilter implements Filter {
  private final RateLimiter limiter = new RateLimiter();

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String clientIp = extractClientIp(request);

    if (!limiter.allowRequest(clientIp)) {
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      httpResponse.sendError(429, "Too Many Requests");
      return;
    }

    chain.doFilter(request, response);
  }

  private String extractClientIp(ServletRequest request) {
    String forwardedFor = request.getServletContext()
        .getAttribute("X-Forwarded-For") != null
        ? (String) request.getServletContext().getAttribute("X-Forwarded-For")
        : null;
    return forwardedFor != null ? forwardedFor.split(",")[0].trim() : request.getRemoteAddr();
  }

  @Override
  public void init(FilterConfig config) {}

  @Override
  public void destroy() {}
}
