import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitingFilter implements Filter {
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_DURATION_MILLIS = 60_000;
    
    private RateLimiter rateLimiter;
    
    @Override
    public void init(FilterConfig config) throws ServletException {
        rateLimiter = new RateLimiter(MAX_REQUESTS_PER_MINUTE, WINDOW_DURATION_MILLIS);
    }
    
    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientId = extractClientId(httpRequest);
        
        if (rateLimiter.isAllowed(clientId)) {
            chain.doFilter(request, response);
        } else {
            sendRateLimitExceeded(httpResponse);
        }
    }
    
    private String extractClientId(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    private void sendRateLimitExceeded(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Rate limit exceeded. Max " 
            + MAX_REQUESTS_PER_MINUTE + " requests per minute.\"}");
    }
    
    @Override
    public void destroy() {
    }
}
