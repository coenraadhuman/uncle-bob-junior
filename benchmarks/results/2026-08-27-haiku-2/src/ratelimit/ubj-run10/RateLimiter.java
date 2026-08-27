import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
  private static final long REFILL_INTERVAL_MS = 60_000; // 1 minute
  private static final int MAX_TOKENS = 5;
  
  private long lastRefillTime;
  private int availableTokens;
  
  public RateLimiter() {
    this.lastRefillTime = System.currentTimeMillis();
    this.availableTokens = MAX_TOKENS;
  }
  
  public synchronized boolean tryConsume() {
    refillTokens();
    if (availableTokens > 0) {
      availableTokens--;
      return true;
    }
    return false;
  }
  
  private void refillTokens() {
    long now = System.currentTimeMillis();
    long timeSinceRefill = now - lastRefillTime;
    
    if (timeSinceRefill >= REFILL_INTERVAL_MS) {
      availableTokens = MAX_TOKENS;
      lastRefillTime = now;
    }
  }
}
