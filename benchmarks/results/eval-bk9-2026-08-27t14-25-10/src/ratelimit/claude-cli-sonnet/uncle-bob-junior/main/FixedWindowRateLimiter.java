package com.example.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public final class FixedWindowRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration windowDuration;
    private final Clock clock;
    private final ConcurrentHashMap<String, ClientWindow> windowsByClientId = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(int maxRequestsPerWindow, Duration windowDuration, Clock clock) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowDuration = windowDuration;
        this.clock = clock;
    }

    public boolean allowRequest(String clientId) {
        Instant now = clock.instant();
        ClientWindow updatedWindow = windowsByClientId.compute(clientId,
                (id, currentWindow) -> nextWindow(currentWindow, now));
        return updatedWindow.requestCount() <= maxRequestsPerWindow;
    }

    public Duration windowDuration() {
        return windowDuration;
    }

    private ClientWindow nextWindow(ClientWindow currentWindow, Instant now) {
        if (currentWindow == null || isExpired(currentWindow, now)) {
            return new ClientWindow(now, 1);
        }
        return currentWindow.incremented();
    }

    private boolean isExpired(ClientWindow window, Instant now) {
        return now.isAfter(window.startedAt().plus(windowDuration));
    }

    private record ClientWindow(Instant startedAt, int requestCount) {
        ClientWindow incremented() {
            return new ClientWindow(startedAt, requestCount + 1);
        }
    }
}
