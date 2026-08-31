class RateLimiter {
  private static final int MAX_REQUESTS = 5;
  private static final long WINDOW_MILLIS = 60_000;
  
  private final Clock clock;
  private final Map<String, ClientWindow> windows;
  
  RateLimiter() {
    this(() -> System.currentTimeMillis());
  }
  
  RateLimiter(Clock clock) {
    this.clock = clock;
    this.windows = new ConcurrentHashMap<>();
  }
  
  boolean allowRequest(String clientId) {
    long now = clock.millis();
    windows.values().removeIf(w -> w.isExpired(now));
    
    ClientWindow window = windows.computeIfAbsent(clientId, id -> new ClientWindow(now));
    return window.tryRecord(now);
  }
  
  private static class ClientWindow {
    private long windowStart;
    private int count;
    
    ClientWindow(long start) {
      this.windowStart = start;
    }
    
    synchronized boolean tryRecord(long now) {
      if (now - windowStart > WINDOW_MILLIS) {
        windowStart = now;
        count = 0;
      }
      
      if (count < MAX_REQUESTS) {
        count++;
        return true;
      }
      return false;
    }
    
    synchronized boolean isExpired(long now) {
      return now - windowStart > WINDOW_MILLIS;
    }
  }
}
