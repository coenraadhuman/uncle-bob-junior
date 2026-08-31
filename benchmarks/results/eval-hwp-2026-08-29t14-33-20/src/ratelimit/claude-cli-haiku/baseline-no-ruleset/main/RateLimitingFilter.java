import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RateLimitingFilter extends HttpFilter {
    private static final RateLimiter rateLimiter = new RateLimiter(5, 60_000);

    static {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, runnable -> {
            Thread thread = new Thread(runnable, "RateLimiter-Cleanup");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(rateLimiter::cleanup, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response,
                           FilterChain chain) throws IOException, ServletException {
        String clientIp = getClientIp(request);

        if (!rateLimiter.allowRequest(clientIp)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Max 5 per minute.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
