// TokenBucket.java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Not thread-safe on its own; callers must synchronize access to a single instance. */
final class TokenBucket {

    private final int capacityTokens;
    private final int refillTokens;
    private final Duration refillPeriod;
    private final Clock clock;

    private double availableTokens;
    private Instant lastRefillTime;

    TokenBucket(int capacityTokens, int refillTokens, Duration refillPeriod, Clock clock) {
        this.capacityTokens = capacityTokens;
        this.refillTokens = refillTokens;
        this.refillPeriod = refillPeriod;
        this.clock = clock;
        this.availableTokens = capacityTokens;
        this.lastRefillTime = clock.instant();
    }

    synchronized boolean tryConsume() {
        refill();
        if (availableTokens < 1) {
            return false;
        }
        availableTokens -= 1;
        return true;
    }

    private void refill() {
        Instant now = clock.instant();
        double periodsElapsed = Duration.between(lastRefillTime, now).toNanos()
                / (double) refillPeriod.toNanos();
        double tokensToAdd = periodsElapsed * refillTokens;
        if (tokensToAdd <= 0) {
            return;
        }
        availableTokens = Math.min(capacityTokens, availableTokens + tokensToAdd);
        lastRefillTime = now;
    }
}
