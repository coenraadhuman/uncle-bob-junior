// ClientRateLimiter.java
import java.util.*;
import java.util.concurrent.*;

public class ClientRateLimiter {
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = 60_000;
    
    private final Map<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();
    private final Supplier<Long> timeProvider;

    public ClientRateLimiter() {
        this(System::currentTimeMillis);
    }

    ClientRateLimiter(Supplier<Long> timeProvider) {
        this.timeProvider = timeProvider;
    }

    public boolean isAllowed(String clientIp) {
        long now = timeProvider.get();
        Deque<Long> timestamps = requestTimestamps.computeIfAbsent(clientIp, k -> new ConcurrentLinkedDeque<>());
        removeExpired(timestamps, now);
        
        if (timestamps.size() < MAX_REQUESTS) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }

    private void removeExpired(Deque<Long> timestamps, long now) {
        while (!timestamps.isEmpty() && timestamps.peekFirst() < now - WINDOW_MILLIS) {
            timestamps.removeFirst();
        }
    }

    int remaining(String clientIp) {
        long now = timeProvider.get();
        Deque<Long> timestamps = requestTimestamps.get(clientIp);
        if (timestamps == null) {
            return MAX_REQUESTS;
        }
        removeExpired(timestamps, now);
        return Math.max(0, MAX_REQUESTS - timestamps.size());
    }
}
