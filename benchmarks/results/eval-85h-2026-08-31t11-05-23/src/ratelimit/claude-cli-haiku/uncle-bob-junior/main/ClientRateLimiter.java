public class ClientRateLimiter {
    private final long requestsPerMinute;
    private final ConcurrentHashMap<String, RateLimiter> clientLimiters = new ConcurrentHashMap<>();
    
    public ClientRateLimiter(long requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }
    
    public boolean allowRequest(String clientId) {
        RateLimiter limiter = clientLimiters.computeIfAbsent(
            clientId,
            key -> new RateLimiter(requestsPerMinute)
        );
        return limiter.allowRequest();
    }
}

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
