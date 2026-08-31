package ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public final class FixedWindowRateLimiter implements ClientRateLimiter {

    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, RequestWindow> windowsByClient = new ConcurrentHashMap<>();
    private final Clock clock;

    public FixedWindowRateLimiter() {
        this(Clock.systemUTC());
    }

    public FixedWindowRateLimiter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean allowRequest(String clientId) {
        Instant now = clock.instant();
        RequestWindow updatedWindow = windowsByClient.compute(clientId,
                (id, existingWindow) -> nextWindow(existingWindow, now));
        return updatedWindow.requestCount() <= MAX_REQUESTS_PER_WINDOW;
    }

    private RequestWindow nextWindow(RequestWindow existingWindow, Instant now) {
        if (existingWindow == null || existingWindow.hasExpired(now, WINDOW_DURATION)) {
            return new RequestWindow(now, 1);
        }
        return existingWindow.incremented();
    }

    private record RequestWindow(Instant startedAt, int requestCount) {

        boolean hasExpired(Instant now, Duration windowDuration) {
            return now.isAfter(startedAt.plus(windowDuration));
        }

        RequestWindow incremented() {
            return new RequestWindow(startedAt, requestCount + 1);
        }
    }
}
