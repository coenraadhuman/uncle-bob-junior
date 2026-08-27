public class RateLimiter {
  private static final long REFILL_INTERVAL_MS = 60_000;
  private static final int MAX_TOKENS = 5;
  
  private long lastRefillTime;
  private int availableTokens;
  private long lastAccessTime;
  
  public RateLimiter() {
    this.lastRefillTime = System.currentTimeMillis();
    this.lastAccessTime = this.lastRefillTime;
    this.availableTokens = MAX_TOKENS;
  }
  
  public synchronized boolean tryConsume() {
    lastAccessTime = System.currentTimeMillis();
    refillTokens();
    if (availableTokens > 0) {
      availableTokens--;
      return true;
    }
    return false;
  }
  
  public long getLastAccessTime() {
    return lastAccessTime;
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
