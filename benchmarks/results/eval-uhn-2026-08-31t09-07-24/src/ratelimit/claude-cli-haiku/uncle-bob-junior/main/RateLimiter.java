import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;

class RateLimiter {
  private static final int REQUESTS_PER_MINUTE = 5;
  private static final long WINDOW_DURATION_MILLIS = 60_000;

  private final Map<String, Queue<Long>> requestsByClient = new ConcurrentHashMap<>();

  boolean isAllowed(String clientId) {
    Queue<Long> timestamps = requestsByClient.computeIfAbsent(clientId, _ -> new LinkedList<>());
    long now = System.currentTimeMillis();
    long windowStart = now - WINDOW_DURATION_MILLIS;

    while (!timestamps.isEmpty() && timestamps.peek() < windowStart) {
      timestamps.poll();
    }

    if (timestamps.size() >= REQUESTS_PER_MINUTE) {
      return false;
    }

    timestamps.offer(now);
    return true;
  }
}
