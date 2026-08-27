import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
  private static final int MAX_REQUESTS_PER_MINUTE = 10;
  private static final long WINDOW_MILLIS = 60_000;
  private static final long CLEANUP_INTERVAL_MILLIS = 300_000;

  private final Map<String, RequestTimestamps> clientRequests;
  private long lastCleanupTime;

  public RateLimiter() {
    this.clientRequests = new ConcurrentHashMap<>();
    this.lastCleanupTime = System.currentTimeMillis();
  }

  public boolean allowRequest(String clientIp) {
    long now = System.currentTimeMillis();
    cleanupIfNeeded(now);

    RequestTimestamps timestamps = clientRequests.computeIfAbsent(
        clientIp,
        k -> new RequestTimestamps()
    );

    return timestamps.recordRequest(now);
  }

  private void cleanupIfNeeded(long now) {
    if (now - lastCleanupTime > CLEANUP_INTERVAL_MILLIS) {
      clientRequests.forEach((clientIp, timestamps) -> {
        if (timestamps.isExpired(now, WINDOW_MILLIS)) {
          clientRequests.remove(clientIp);
        }
      });
      lastCleanupTime = now;
    }
  }

  private static class RequestTimestamps {
    private final Queue<Long> timestamps = new LinkedList<>();

    boolean recordRequest(long now) {
      removeExpiredTimestamps(now);

      if (timestamps.size() < MAX_REQUESTS_PER_MINUTE) {
        timestamps.add(now);
        return true;
      }

      return false;
    }

    private void removeExpiredTimestamps(long now) {
      while (!timestamps.isEmpty() && now - timestamps.peek() > WINDOW_MILLIS) {
        timestamps.poll();
      }
    }

    boolean isExpired(long now, long windowMillis) {
      return timestamps.isEmpty() || now - timestamps.peek() > windowMillis;
    }
  }
}
