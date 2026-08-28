package ratelimit;

public interface ClientRateLimiter {
    boolean allowRequest(String clientId);
}
