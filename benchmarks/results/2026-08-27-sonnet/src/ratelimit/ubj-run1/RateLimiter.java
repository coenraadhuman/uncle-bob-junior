package ratelimit;

/**
 * Decides whether a client identified by {@code clientId} may proceed with a request.
 * Implementations must be thread-safe.
 */
public interface RateLimiter {
    boolean tryAcquire(String clientId);
}
