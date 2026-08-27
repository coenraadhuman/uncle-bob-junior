import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class RateLimitedServlet extends HttpServlet {
    private static final int REQUESTS_PER_MINUTE = 10;
    private final RateLimiter rateLimiter = new RateLimiter(REQUESTS_PER_MINUTE);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String clientIp = getClientIp(request);
        
        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setContentType("text/plain");
            response.getWriter().println("Rate limit exceeded: max " + REQUESTS_PER_MINUTE + " requests per minute");
            return;
        }
        
        // Your actual request handling here
        response.setContentType("text/plain");
        response.getWriter().println("Request successful");
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
