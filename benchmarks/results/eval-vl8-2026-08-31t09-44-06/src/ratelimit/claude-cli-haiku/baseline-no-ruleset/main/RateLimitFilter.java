import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitFilter implements Filter {
    private RateLimiter rateLimiter;

    @Override
    public void init(FilterConfig config) throws ServletException {
        rateLimiter = new RateLimiter(10, 60_000); // 10 requests per minute
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = httpRequest.getRemoteAddr();

        if (!rateLimiter.allowRequest(clientIp)) {
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.getWriter().write("{\"error\": \"Rate limit exceeded. Max 10 requests per minute.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
