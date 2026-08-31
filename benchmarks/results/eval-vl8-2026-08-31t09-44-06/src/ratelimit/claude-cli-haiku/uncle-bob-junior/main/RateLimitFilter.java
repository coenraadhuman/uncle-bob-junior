import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RateLimitFilter implements Filter {
    private static final String TOO_MANY_REQUESTS = "Too many requests";
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final long WINDOW_MILLIS = 60_000;
    
    private RateLimiter rateLimiter;
    
    @Override
    public void init(FilterConfig config) {
        this.rateLimiter = new RateLimiter(MAX_REQUESTS_PER_MINUTE, WINDOW_MILLIS);
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientId = getClientId(httpRequest);
        
        if (!rateLimiter.allowRequest(clientId)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HTTP_TOO_MANY_REQUESTS);
            httpResponse.getWriter().write(TOO_MANY_REQUESTS);
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {}
    
    private String getClientId(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
