// RateLimiter.java
public interface RateLimiter {
    boolean tryAcquire(String clientId);
}
