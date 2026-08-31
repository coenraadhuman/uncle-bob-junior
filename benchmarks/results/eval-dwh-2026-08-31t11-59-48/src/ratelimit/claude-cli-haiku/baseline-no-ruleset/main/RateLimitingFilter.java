import javax.servlet.*;
import javax.servlet.http.*;

public class RateLimitingFilter implements Filter {
    private RateLimiter rateLimiter;

    @Override
    public void init(FilterConfig config) {
        rateLimiter = new RateLimiter(10); // 10 requests per minute
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientId = getClientIp(httpRequest);

        if (!rateLimiter.allowRequest(clientId)) {
            httpResponse.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            httpResponse.setHeader("Retry-After", "60");
            response.getWriter().write("Rate limit exceeded. Max 10 requests per minute.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        rateLimiter.shutdown();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
