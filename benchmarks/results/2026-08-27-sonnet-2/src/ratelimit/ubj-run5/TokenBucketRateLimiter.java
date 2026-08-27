// TokenBucketRateLimiter.java
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class TokenBucketRateLimiter implements RateLimiter {

    public static final int DEFAULT_MAX_REQUESTS_PER_MINUTE = 5;
    public static final Duration DEFAULT_REFILL_PERIOD = Duration.ofMinutes(1);

    private final int capacityTokens;
    private final int refillTokens;
    private final Duration refillPeriod;
    private final Clock clock;

    // ubj: unbounded per-client map; fine for a small/known client set.
    // Add eviction of idle buckets if the distinct-client population grows unbounded.
    private final ConcurrentMap<String, TokenBucket> bucketsByClient = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(int capacityTokens, int refillTokens, Duration refillPeriod, Clock clock) {
        this.capacityTokens = capacityTokens;
        this.refillTokens = refillTokens;
        this.refillPeriod = refillPeriod;
        this.clock = clock;
    }

    public static TokenBucketRateLimiter withDefaultLimit() {
        return new TokenBucketRateLimiter(
                DEFAULT_MAX_REQUESTS_PER_MINUTE, DEFAULT_MAX_REQUESTS_PER_MINUTE,
                DEFAULT_REFILL_PERIOD, Clock.systemUTC());
    }

    @Override
    public boolean allowRequest(String clientId) {
        return bucketsByClient
                .computeIfAbsent(clientId, id -> new TokenBucket(capacityTokens, refillTokens, refillPeriod, clock))
                .tryConsume();
    }
}
