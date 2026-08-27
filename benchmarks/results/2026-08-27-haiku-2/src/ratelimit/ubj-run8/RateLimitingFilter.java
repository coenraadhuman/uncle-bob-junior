import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitingFilter implements Filter {
  private final ClientRateLimiter rateLimiter;

  public RateLimitingFilter() {
    this.rateLimiter = new ClientRateLimiter();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String clientIp = getClientIp(request);

    if (!rateLimiter.allowRequest(clientIp)) {
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      httpResponse.setStatus(429);
      httpResponse.setContentType("application/json");
      httpResponse.getWriter().write("{\"error\":\"Rate limit exceeded. Maximum 5 requests per minute.\"}");
      return;
    }

    chain.doFilter(request, response);
  }

  private String getClientIp(ServletRequest request) {
    String forwardedFor = request.getServletContext().getAttribute("X-Forwarded-For") != null
        ? (String) request.getServletContext().getAttribute("X-Forwarded-For")
        : null;

    if (forwardedFor != null && !forwardedFor.isEmpty()) {
      return forwardedFor.split(",")[0].trim();
    }

    return request.getRemoteAddr();
  }
}
