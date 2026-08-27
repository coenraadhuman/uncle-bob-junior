import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiter {
    private static class ClientQuota {
        AtomicLong tokens;
        long lastRefillTime;

        ClientQuota(long initialTokens) {
            this.tokens = new AtomicLong(initialTokens);
            this.lastRefillTime = System.currentTimeMillis();
        }
    }

    private final ConcurrentHashMap<String, ClientQuota> quotas = new ConcurrentHashMap<>();
    private final long maxTokens;
    private final long refillIntervalMs;
    private final long tokensPerInterval;

    public RateLimiter(long requestsPerMinute) {
        this.maxTokens = requestsPerMinute;
        this.refillIntervalMs = 60_000; // 1 minute
        this.tokensPerInterval = requestsPerMinute;
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        ClientQuota quota = quotas.computeIfAbsent(clientId, k -> new ClientQuota(maxTokens));

        synchronized (quota) {
            long timePassed = now - quota.lastRefillTime;
            if (timePassed >= refillIntervalMs) {
                quota.tokens.set(maxTokens);
                quota.lastRefillTime = now;
            }

            if (quota.tokens.get() > 0) {
                quota.tokens.decrementAndGet();
                return true;
            }
        }
        return false;
    }

    public void cleanupExpiredQuotas(long expirationTimeMs) {
        long now = System.currentTimeMillis();
        quotas.entrySet().removeIf(entry -> 
            now - entry.getValue().lastRefillTime > expirationTimeMs
        );
    }
}
