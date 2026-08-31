import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitFilter implements Filter {
    private RateLimiter rateLimiter;
    
    @Override
    public void init(FilterConfig config) {
        this.rateLimiter = new RateLimiter(5); // 5 requests per minute
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        String clientIp = request.getRemoteAddr();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setHeader("Retry-After", "12");
            httpResponse.sendError(429, "Too Many Requests");
            return;
        }
        
        chain.doFilter(request, response);
    }
}
