import com.sun.net.httpserver.HttpExchange;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RateLimiter {
    private static class TokenBucket {
        private double tokens;
        private long lastRefillTime;
        private final double capacity;
        private final double refillRate;
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        TokenBucket(double capacity, double refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }

        boolean tryConsume(int tokens) {
            lock.writeLock().lock();
            try {
                refill();
                if (this.tokens >= tokens) {
                    this.tokens -= tokens;
                    return true;
                }
                return false;
            } finally {
                lock.writeLock().unlock();
            }
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            double tokensToAdd = (elapsed / 1000.0) * refillRate;
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }
    }

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final double requestsPerSecond;
    private final int capacity;

    public RateLimiter(int requestsPerMinute) {
        this.requestsPerSecond = requestsPerMinute / 60.0;
        this.capacity = requestsPerMinute;
    }

    public boolean allowRequest(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, 
            k -> new TokenBucket(capacity, requestsPerSecond));
        return bucket.tryConsume(1);
    }

    public String getClientId(HttpExchange exchange) {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}
