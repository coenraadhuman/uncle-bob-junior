package ratelimit;

import java.time.Duration;

/** Outcome of a rate-limit check; {@code retryAfter} is zero when the request is allowed. */
public record RateLimitDecision(boolean allowed, Duration retryAfter) {

    public static RateLimitDecision allowed() {
        return new RateLimitDecision(true, Duration.ZERO);
    }

    public static RateLimitDecision rejectedFor(Duration retryAfter) {
        return new RateLimitDecision(false, retryAfter);
    }
}
