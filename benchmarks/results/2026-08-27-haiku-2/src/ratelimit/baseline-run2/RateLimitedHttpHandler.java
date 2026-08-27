import javax.servlet.*;
import javax.servlet.http.*;

public class RateLimitedHttpHandler extends HttpServlet {
    private static final RateLimiter rateLimiter = new RateLimiter(5); // 5 requests/min

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {
        String clientIp = getClientIp(request);
        
        if (!rateLimiter.isAllowed(clientIp)) {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setHeader("Retry-After", "60");
            response.getWriter().write("Rate limit exceeded. Max 5 requests per minute.");
            return;
        }

        // Add rate limit info headers
        int remaining = rateLimiter.getRemainingRequests(clientIp);
        response.setHeader("X-RateLimit-Limit", "5");
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", 
            String.valueOf(System.currentTimeMillis() + 60_000));

        // Your actual handler logic here
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Request processed successfully");
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
