import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class ClientRateLimiter {
  private static final int DEFAULT_MAX_REQUESTS = 5;
  private static final int DEFAULT_WINDOW_MINUTES = 1;

  private final int maxRequests;
  private final long windowMillis;
  private final Supplier<Long> timeProvider;
  private final Map<String, Queue<Long>> clientRequests = new ConcurrentHashMap<>();

  public ClientRateLimiter() {
    this(DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_MINUTES);
  }

  public ClientRateLimiter(int maxRequests, int windowMinutes) {
    this(maxRequests, windowMinutes, System::currentTimeMillis);
  }

  ClientRateLimiter(int maxRequests, int windowMinutes, Supplier<Long> timeProvider) {
    this.maxRequests = maxRequests;
    this.windowMillis = (long) windowMinutes * 60 * 1000;
    this.timeProvider = timeProvider;
  }

  public boolean allowRequest(String clientId) {
    long now = timeProvider.get();
    Queue<Long> timestamps = clientRequests.computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<>());

    while (!timestamps.isEmpty() && now - timestamps.peek() > windowMillis) {
      timestamps.poll();
    }

    if (timestamps.size() < maxRequests) {
      timestamps.offer(now);
      return true;
    }
    return false;
  }
}
