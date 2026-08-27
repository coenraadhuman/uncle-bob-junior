import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimiter rateLimiter = new RateLimiter(10, 60);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientId = getClientIp(request);
        if (!rateLimiter.allowRequest(clientId)) {
            response.setStatus(429);
            response.getWriter().write("{\"error\":\"Too many requests. Max 10 requests per minute.\"}");
            response.setContentType("application/json");
            return false;
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

class RateLimiter {
    private static class ClientBucket {
        long lastResetTime;
        int requestCount;

        ClientBucket() {
            this.lastResetTime = System.currentTimeMillis();
            this.requestCount = 0;
        }
    }

    private final int maxRequests;
    private final int windowSeconds;
    private final ConcurrentHashMap<String, ClientBucket> buckets = new ConcurrentHashMap<>();

    RateLimiter(int maxRequests, int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds * 1000;
    }

    boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        ClientBucket bucket = buckets.compute(clientId, (id, existing) -> {
            if (existing == null) {
                return new ClientBucket();
            }
            if (now - existing.lastResetTime > windowSeconds) {
                existing.lastResetTime = now;
                existing.requestCount = 0;
            }
            return existing;
        });

        if (bucket.requestCount < maxRequests) {
            bucket.requestCount++;
            return true;
        }
        return false;
    }
}
