import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClientRateLimiterTest {

    private static final int LIMIT = 3;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String CLIENT_A = "client-a";
    private static final String CLIENT_B = "client-b";

    /** Test double: a Clock whose instant can be advanced deterministically. */
    private static final class MutableClock extends Clock {
        private Instant now;
        MutableClock(Instant start) { this.now = start; }
        void advanceBy(Duration duration) { now = now.plus(duration); }
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    @Test
    void allowsRequestsUpToTheLimit() {
        ClientRateLimiter limiter = new ClientRateLimiter(LIMIT, WINDOW, new MutableClock(Instant.EPOCH));

        for (int i = 0; i < LIMIT; i++) {
            assertTrue(limiter.tryAcquire(CLIENT_A), "request " + i + " should be allowed");
        }
    }

    @Test
    void blocksRequestsBeyondTheLimit() {
        ClientRateLimiter limiter = new ClientRateLimiter(LIMIT, WINDOW, new MutableClock(Instant.EPOCH));

        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire(CLIENT_A);
        }

        assertFalse(limiter.tryAcquire(CLIENT_A));
    }

    @Test
    void tracksEachClientIndependently() {
        ClientRateLimiter limiter = new ClientRateLimiter(LIMIT, WINDOW, new MutableClock(Instant.EPOCH));

        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire(CLIENT_A);
        }

        assertTrue(limiter.tryAcquire(CLIENT_B), "a different client must have its own quota");
    }

    @Test
    void resetsQuotaAfterWindowElapses() {
        MutableClock clock = new MutableClock(Instant.EPOCH);
        ClientRateLimiter limiter = new ClientRateLimiter(LIMIT, WINDOW, clock);

        for (int i = 0; i < LIMIT; i++) {
            limiter.tryAcquire(CLIENT_A);
        }
        assertFalse(limiter.tryAcquire(CLIENT_A));

        clock.advanceBy(WINDOW.plusSeconds(1));

        assertTrue(limiter.tryAcquire(CLIENT_A), "quota should reset once the window has passed");
    }

    @Test
    void rejectsInvalidLimitAtConstruction() {
        assertThrowsIllegalArgument(() -> new ClientRateLimiter(0, WINDOW, Clock.systemUTC()));
    }

    private static void assertThrowsIllegalArgument(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("expected IllegalArgumentException");
    }
}

class RateLimitingHttpHandlerTest {

    private static final String CLIENT_IP = "203.0.113.7";

    @Test
    void delegatesWhenClientIsWithinQuota() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        ClientRateLimiter rateLimiter = new ClientRateLimiter(5, Duration.ofMinutes(1), Clock.systemUTC());
        RateLimitingHttpHandler handler =
                new RateLimitingHttpHandler(delegate, rateLimiter, Duration.ofMinutes(1));
        HttpExchange exchange = exchangeFrom(CLIENT_IP);

        handler.handle(exchange);

        verify(delegate, times(1)).handle(exchange);
        verify(exchange, never()).sendResponseHeaders(any(Integer.class) == null ? 0 : 429, -1);
    }

    @Test
    void rejectsWithTooManyRequestsOnceQuotaIsExhausted() throws Exception {
        HttpHandler delegate = mock(HttpHandler.class);
        ClientRateLimiter rateLimiter = new ClientRateLimiter(1, Duration.ofMinutes(1), Clock.systemUTC());
        RateLimitingHttpHandler handler =
                new RateLimitingHttpHandler(delegate, rateLimiter, Duration.ofMinutes(1));

        handler.handle(exchangeFrom(CLIENT_IP));
        HttpExchange secondExchange = exchangeFrom(CLIENT_IP);
        handler.handle(secondExchange);

        verify(delegate, times(1)).handle(any(HttpExchange.class));
        verify(secondExchange).sendResponseHeaders(429, -1);
    }

    private HttpExchange exchangeFrom(String ip) {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRemoteAddress()).thenReturn(new InetSocketAddress(ip, 54321));
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        return exchange;
    }
}
