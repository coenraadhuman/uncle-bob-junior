import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

public class RateLimiter {
  private static final int MAX_REQUESTS = 10;
  private static final long WINDOW_MINUTES = 1;
  private static final long WINDOW_MILLIS = WINDOW_MINUTES * 60 * 1000;
  
  private final ConcurrentHashMap<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();
  
  public boolean allowRequest(String clientId) {
    long now = System.currentTimeMillis();
    Deque<Long> timestamps = requestTimestamps.computeIfAbsent(
        clientId,
        k -> new LinkedBlockingDeque<>()
    );
    
    removeExpiredRequests(timestamps, now);
    
    if (timestamps.size() < MAX_REQUESTS) {
      timestamps.addLast(now);
      return true;
    }
    
    return false;
  }
  
  private void removeExpiredRequests(Deque<Long> timestamps, long now) {
    while (!timestamps.isEmpty() && timestamps.getFirst() <= now - WINDOW_MILLIS) {
      timestamps.removeFirst();
    }
  }
}
