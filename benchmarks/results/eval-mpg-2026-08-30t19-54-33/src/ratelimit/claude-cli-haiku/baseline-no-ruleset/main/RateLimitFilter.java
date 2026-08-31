import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RateLimitFilter implements Filter {
    private RateLimiter rateLimiter;

    @Override
    public void init(FilterConfig config) {
        // 10 requests per 60 seconds
        rateLimiter = new RateLimiter(10, TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientId = getClientIp(httpRequest);

        if (!rateLimiter.allowRequest(clientId)) {
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setHeader("Retry-After", "60");
            response.getWriter().write("Rate limit exceeded. Max 10 requests per minute.");
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    public void destroy() {}
}
