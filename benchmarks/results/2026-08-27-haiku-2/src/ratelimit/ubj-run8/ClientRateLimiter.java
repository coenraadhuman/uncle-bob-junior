import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClientRateLimiter {
  private static final int REQUESTS_PER_MINUTE = 5;
  private static final long MINUTE_MILLIS = 60_000;
  private static final long CLEANUP_INTERVAL_MILLIS = 300_000;

  private final ConcurrentHashMap<String, ClientRequestBucket> buckets;
  private final ReentrantReadWriteLock lock;
  private long lastCleanup;

  public ClientRateLimiter() {
    this.buckets = new ConcurrentHashMap<>();
    this.lock = new ReentrantReadWriteLock();
    this.lastCleanup = System.currentTimeMillis();
  }

  public boolean allowRequest(String clientId) {
    lock.readLock().lock();
    try {
      ClientRequestBucket bucket = buckets.get(clientId);
      long now = System.currentTimeMillis();

      if (bucket == null || bucket.hasExpired(now)) {
        lock.readLock().unlock();
        lock.writeLock().lock();
        try {
          buckets.put(clientId, new ClientRequestBucket(now));
          maybeCleanup(now);
          return true;
        } finally {
          lock.readLock().lock();
          lock.writeLock().unlock();
        }
      }

      boolean allowed = bucket.tryConsumeRequest(now);
      if (allowed) {
        maybeCleanup(now);
      }
      return allowed;
    } finally {
      lock.readLock().unlock();
    }
  }

  private void maybeCleanup(long now) {
    if (now - lastCleanup > CLEANUP_INTERVAL_MILLIS) {
      buckets.entrySet().removeIf(e -> e.getValue().hasExpired(now));
      lastCleanup = now;
    }
  }

  private static class ClientRequestBucket {
    private final long windowStart;
    private int requestCount;

    ClientRequestBucket(long windowStart) {
      this.windowStart = windowStart;
      this.requestCount = 1;
    }

    boolean hasExpired(long now) {
      return now - windowStart > MINUTE_MILLIS;
    }

    boolean tryConsumeRequest(long now) {
      if (now - windowStart > MINUTE_MILLIS) {
        windowStart = now;
        requestCount = 1;
        return true;
      }
      if (requestCount < REQUESTS_PER_MINUTE) {
        requestCount++;
        return true;
      }
      return false;
    }
  }
}
