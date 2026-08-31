import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class RateLimitFilter implements Filter {
    private RateLimiter rateLimiter;
    
    @Override
    public void init(FilterConfig config) {
        rateLimiter = new RateLimiter(10, 60_000);
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientId = httpRequest.getRemoteAddr();
        
        if (!rateLimiter.allowRequest(clientId)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(429, "Too Many Requests");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {}
}
