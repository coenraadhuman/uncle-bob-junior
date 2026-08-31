import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitFilter implements Filter {
    private RateLimiter rateLimiter;
    
    @Override
    public void init(FilterConfig config) {
        // 5 requests per minute (60,000 milliseconds)
        rateLimiter = new RateLimiter(5, 60_000);
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        String clientIp = request.getRemoteAddr();
        
        if (!rateLimiter.allowRequest(clientIp)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            httpResponse.setHeader("Retry-After", "60");
            response.getWriter().write("Rate limit exceeded");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {}
}
