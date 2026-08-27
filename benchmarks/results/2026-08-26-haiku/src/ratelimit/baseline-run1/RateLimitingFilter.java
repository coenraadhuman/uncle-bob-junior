import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitingFilter implements Filter {
    private RateLimiter rateLimiter;

    @Override
    public void init(FilterConfig config) throws ServletException {
        // 5 requests per 60 seconds per client
        rateLimiter = new RateLimiter(5, 60000);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        String clientId = getClientIp((HttpServletRequest) request);
        
        if (!rateLimiter.isAllowed(clientId)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setHeader("Retry-After", "60");
            httpResponse.getWriter().write("Rate limit exceeded");
            return;
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        rateLimiter.shutdown();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
