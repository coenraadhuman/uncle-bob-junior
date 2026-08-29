import java.util.*;
import java.util.concurrent.*;

public class RateLimiter {
  private static final int MAX_REQUESTS = 5;
  private static final long WINDOW_MILLIS = 60_000;
  private static final long CLEANUP_INTERVAL_MILLIS = 300_000;

  private final Map<String, Deque<Long>> requestsByClient = new ConcurrentHashMap<>();
  private volatile long lastCleanup = System.currentTimeMillis();

  public boolean allowRequest(String clientId) {
    cleanupIfStale();
    long now = System.currentTimeMillis();
    Deque<Long> timestamps = requestsByClient.computeIfAbsent(clientId, _ -> new ConcurrentLinkedDeque<>());

    removeExpiredTimestamps(timestamps, now);
    if (timestamps.size() >= MAX_REQUESTS) {
      return false;
    }

    timestamps.add(now);
    return true;
  }

  private void removeExpiredTimestamps(Deque<Long> timestamps, long now) {
    while (!timestamps.isEmpty() && now - timestamps.getFirst() >= WINDOW_MILLIS) {
      timestamps.removeFirst();
    }
  }

  private void cleanupIfStale() {
    long now = System.currentTimeMillis();
    if (now - lastCleanup < CLEANUP_INTERVAL_MILLIS) {
      return;
    }

    lastCleanup = now;
    requestsByClient.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }
}
