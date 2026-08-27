import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitingFilter implements Filter {
    private ClientRateLimiter rateLimiter;
    private static final int MAX_REQUESTS_PER_MINUTE = 10;

    @Override
    public void init(FilterConfig config) {
        rateLimiter = new ClientRateLimiter(MAX_REQUESTS_PER_MINUTE, 60);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientId = getClientIp(httpRequest);
        if (!rateLimiter.allowRequest(clientId)) {
            httpResponse.setStatus(429);
            httpResponse.setHeader("Retry-After", "60");
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Max 10 requests per minute.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        rateLimiter.shutdown();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
