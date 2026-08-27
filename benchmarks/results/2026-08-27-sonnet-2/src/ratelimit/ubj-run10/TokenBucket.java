import java.time.Clock;
import java.time.Duration;

final class TokenBucket {

    private final int capacity;
    private final double tokensPerMillisecond;
    private final Clock clock;

    private double availableTokens;
    private long lastRefillTimestampMillis;

    TokenBucket(int capacity, Duration refillWindow, Clock clock) {
        this.capacity = capacity;
        this.tokensPerMillisecond = (double) capacity / refillWindow.toMillis();
        this.clock = clock;
        this.availableTokens = capacity;
        this.lastRefillTimestampMillis = clock.millis();
    }

    synchronized boolean tryConsume() {
        refill();
        if (availableTokens < 1.0) {
            return false;
        }
        availableTokens -= 1.0;
        return true;
    }

    synchronized boolean isIdleAsOf(long nowMillis, long idleThresholdMillis) {
        return nowMillis - lastRefillTimestampMillis >= idleThresholdMillis;
    }

    private void refill() {
        long now = clock.millis();
        long elapsedMillis = now - lastRefillTimestampMillis;
        if (elapsedMillis <= 0) {
            return;
        }
        availableTokens = Math.min(capacity, availableTokens + elapsedMillis * tokensPerMillisecond);
        lastRefillTimestampMillis = now;
    }
}
