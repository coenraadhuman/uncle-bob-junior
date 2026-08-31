I'll provide a rate limiter implementation for Java HTTP handlers using an in-memory token bucket approach. This tracks requests per client IP and allows a configurable number of requests per minute.

```java
import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
    private static class ClientQuota {
        private final Queue<Long> requestTimestamps;
        private final int maxRequests;
        private final long windowMillis;

        ClientQuota(int maxRequests, long windowMillis) {
            this.requestTimestamps = new ConcurrentLinkedQueue<>();
            this.maxRequests = maxRequests;
            this.windowMillis = windowMillis;
        }

        synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            long cutoff = now - windowMillis;

            // Remove timestamps outside the window
            requestTimestamps.removeIf(timestamp -> timestamp < cutoff);

            if (requestTimestamps.size() < maxRequests) {
                requestTimestamps.offer(now);
                return true;
            }
            return false;
        }
    }

    private final Map<String, ClientQuota> quotas = new ConcurrentHashMap<>();
    private final int maxRequestsPerMinute;
    private final long windowMillis;
    private final ScheduledExecutorService cleanup;

    public RateLimiter(int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.windowMillis = 60_000; // 1 minute

        // Clean up stale entries every 10 minutes
        this.cleanup = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "RateLimiter-Cleanup");
            t.setDaemon(true);
            return t;
        });
        cleanup.scheduleAtFixedRate(this::cleanupStaleEntries, 10, 10, TimeUnit.MINUTES);
    }

    public boolean allowRequest(String clientId) {
        ClientQuota quota = quotas.computeIfAbsent(
            clientId,
            k -> new ClientQuota(maxRequestsPerMinute, windowMillis)
        );
        return quota.allowRequest();
    }

    private void cleanupStaleEntries() {
        long now = System.currentTimeMillis();
        quotas.entrySet().removeIf(entry -> {
            Queue<Long> timestamps = entry.getValue().requestTimestamps;
            return timestamps.isEmpty() || 
                   (timestamps.peek() != null && timestamps.peek() < now - windowMillis);
        });
    }

    public void shutdown() {
        cleanup.shutdown();
    }
}
```

**Example usage with a servlet:**

```java
import javax.servlet.*;
import javax.servlet.http.*;

public class RateLimitingFilter implements Filter {
    private RateLimiter rateLimiter;

    @Override
    public void init(FilterConfig config) {
        rateLimiter = new RateLimiter(10); // 10 requests per minute
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientId = getClientIp(httpRequest);

        if (!rateLimiter.allowRequest(clientId)) {
            httpResponse.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            httpResponse.setHeader("Retry-After", "60");
            response.getWriter().write("Rate limit exceeded. Max 10 requests per minute.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        rateLimiter.shutdown();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
```

**Assumptions:**
- Rate limit set to 10 requests per minute per client (adjust `maxRequestsPerMinute` as needed)
- Client identified by IP address (checks `X-Forwarded-For` header for proxied requests)
- In-memory storage with periodic cleanup of stale entries
- Returns HTTP 429 (Too Many Requests) when limit exceeded

The implementation is thread-safe and suitable for production use in high-concurrency environments.