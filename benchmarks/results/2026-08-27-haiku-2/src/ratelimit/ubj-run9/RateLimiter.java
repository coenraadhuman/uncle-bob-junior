public class RateLimiter {
  private static final int MAX_REQUESTS = 10;
  private static final long WINDOW_MILLIS = 60_000;
  private static final long CLEANUP_INTERVAL_MILLIS = 5 * 60_000;

  private final Map<String, Queue<Long>> requestsByClient;
  private final int maxRequests;
  private final long windowSize;
  private final ReentrantReadWriteLock lock;
  private volatile long lastCleanup;

  public RateLimiter(int maxRequests, long windowMillis) {
    this.requestsByClient = new ConcurrentHashMap<>();
    this.maxRequests = maxRequests;
    this.windowSize = windowMillis;
    this.lock = new ReentrantReadWriteLock();
    this.lastCleanup = System.currentTimeMillis();
  }

  public RateLimiter() {
    this(MAX_REQUESTS, WINDOW_MILLIS);
  }

  public boolean allowRequest(String clientId) {
    long now = System.currentTimeMillis();
    
    lock.readLock().lock();
    try {
      Queue<Long> timestamps = requestsByClient.computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<>());
      
      while (!timestamps.isEmpty() && timestamps.peek() < now - windowSize) {
        timestamps.poll();
      }
      
      if (timestamps.size() < maxRequests) {
        timestamps.offer(now);
        return true;
      }
      return false;
    } finally {
      lock.readLock().unlock();
    }
  }

  private void cleanupOldClients(long now) {
    if (now - lastCleanup < CLEANUP_INTERVAL_MILLIS) {
      return;
    }
    
    lock.writeLock().lock();
    try {
      lastCleanup = now;
      requestsByClient.forEach((clientId, timestamps) -> {
        while (!timestamps.isEmpty() && timestamps.peek() < now - windowSize) {
          timestamps.poll();
        }
      });
      
      requestsByClient.entrySet().removeIf(e -> e.getValue().isEmpty());
    } finally {
      lock.writeLock().unlock();
    }
  }
}
