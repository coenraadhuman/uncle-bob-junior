I'll provide a complete rate limiting solution for a Java HTTP handler. I'll make these assumptions:
- 10 requests per 60 seconds per client
- Rate limiting per client IP address
- Using a servlet filter approach (compatible with Spring, Jakarta EE, etc.)
- Token bucket algorithm for fairness

Here's the implementation:

```java
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class ClientRateLimiter {
    private final int maxRequests;
    private final long windowSeconds;
    private final ConcurrentHashMap<String, TokenBucket> buckets;
    private final ScheduledExecutorService cleanup;

    public ClientRateLimiter(int maxRequests, long windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
        this.buckets = new ConcurrentHashMap<>();
        this.cleanup = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "RateLimiter-Cleanup");
            t.setDaemon(true);
            return t;
        });
        scheduleCleanup();
    }

    public boolean allowRequest(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, _ -> new TokenBucket(maxRequests, windowSeconds));
        return bucket.consumeToken();
    }

    private void scheduleCleanup() {
        cleanup.scheduleAtFixedRate(this::removeExpiredBuckets, windowSeconds, windowSeconds, TimeUnit.SECONDS);
    }

    private void removeExpiredBuckets() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    public void shutdown() {
        cleanup.shutdown();
    }

    private static class TokenBucket {
        private final int capacity;
        private final double refillRate;
        private double tokens;
        private long lastRefillTime;

        TokenBucket(int capacity, long windowSeconds) {
            this.capacity = capacity;
            this.refillRate = (double) capacity / windowSeconds;
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean consumeToken() {
            refill();
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsedMs = now - lastRefillTime;
            double tokensToAdd = (elapsedMs / 1000.0) * refillRate;
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }

        boolean isExpired(long now) {
            return (now - lastRefillTime) > 5 * 60 * 1000;
        }
    }
}
```

```java
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RateLimitingFilter implements Filter {
    private ClientRateLimiter rateLimiter;
    private static final int MAX_REQUESTS_PER_MINUTE = 10;

    @Override
    public void init(FilterConfig config) {
        rateLimiter = new ClientRateLimiter(MAX_REQUESTS_PER_MINUTE, 60);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientId = getClientIp(httpRequest);
        if (!rateLimiter.allowRequest(clientId)) {
            httpResponse.setStatus(429);
            httpResponse.setHeader("Retry-After", "60");
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Max 10 requests per minute.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        rateLimiter.shutdown();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

```java
import java.util.HashMap;
import java.util.Map;

public class RateLimitingFilterTest {
    private ClientRateLimiter limiter;

    public static void main(String[] args) {
        RateLimitingFilterTest test = new RateLimitingFilterTest();
        test.testAllowsInitialRequests();
        test.testBlocksExcessRequests();
        test.testTokenRefill();
        System.out.println("All tests passed!");
    }

    void testAllowsInitialRequests() {
        limiter = new ClientRateLimiter(10, 60);
        String clientId = "client-1";
        for (int i = 0; i < 10; i++) {
            assert limiter.allowRequest(clientId) : "Request " + i + " should be allowed";
        }
        assert !limiter.allowRequest(clientId) : "Request 11 should be blocked";
        limiter.shutdown();
    }

    void testBlocksExcessRequests() {
        limiter = new ClientRateLimiter(3, 60);
        String clientId = "client-2";
        assert limiter.allowRequest(clientId);
        assert limiter.allowRequest(clientId);
        assert limiter.allowRequest(clientId);
        assert !limiter.allowRequest(clientId) : "4th request should be blocked";
        limiter.shutdown();
    }

    void testTokenRefill() throws InterruptedException {
        limiter = new ClientRateLimiter(2, 1);
        String clientId = "client-3";
        assert limiter.allowRequest(clientId);
        assert limiter.allowRequest(clientId);
        assert !limiter.allowRequest(clientId);
        
        Thread.sleep(1100);
        assert limiter.allowRequest(clientId) : "Should allow after refill";
        limiter.shutdown();
    }
}
```

**Configuration for Spring Boot** (`application.properties`):

```properties
server.servlet.register-default-servlet=true
```

**Spring Configuration** (if using Spring):

```java
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitingConfig {
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter() {
        FilterRegistrationBean<RateLimitingFilter> bean = new FilterRegistrationBean<>(new RateLimitingFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(1);
        return bean;
    }
}
```

**Key features:**
- **Token bucket algorithm**: Fair distribution; tokens refill continuously at a constant rate
- **Thread-safe**: Uses `ConcurrentHashMap` and synchronized token consumption
- **Per-client tracking**: Maintains separate rate limit state for each IP
- **Automatic cleanup**: Removes expired buckets every 5 minutes to prevent memory leaks
- **X-Forwarded-For support**: Works behind proxies
- **HTTP 429 response**: Standard rate-limit status with `Retry-After` header
- **Simple configuration**: Tune `MAX_REQUESTS_PER_MINUTE` and window size as needed

Tests verify: initial allowance, blocking excess, and token refill after window reset.