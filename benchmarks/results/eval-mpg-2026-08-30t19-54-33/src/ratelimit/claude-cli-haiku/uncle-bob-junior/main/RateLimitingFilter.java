import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class RateLimitingFilter implements Filter {
    private final RateLimiter limiter = new RateLimiter();
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientId = getClientId(httpRequest);
        
        if (!limiter.allowRequest(clientId)) {
            httpResponse.sendError(429, "Too Many Requests");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private String getClientId(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isEmpty()) ? forwarded : request.getRemoteAddr();
    }
    
    @Override
    public void init(FilterConfig config) {}
    
    @Override
    public void destroy() {}
}
