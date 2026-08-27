// RateLimiter.java
public interface RateLimiter {
    boolean allowRequest(String clientId);
}
