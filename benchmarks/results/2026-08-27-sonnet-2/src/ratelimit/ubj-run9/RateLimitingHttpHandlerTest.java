// File: src/test/java/com/example/ratelimit/RateLimitingHttpHandlerTest.java
package com.example.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitingHttpHandlerTest {

    private static final int MAX_REQUESTS_PER_WINDOW = 2;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final int HTTP_OK = 200;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private final FixedWindowRateLimiter rateLimiter =
            new FixedWindowRateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW, clock);
    private final RateLimitingHttpHandler handler =
            new RateLimitingHttpHandler(this::respondOk, rateLimiter, WINDOW);

    @AfterEach
    void tearDown() {
        rateLimiter.close();
    }

    @Test
    void allowsRequestsUpToTheLimit() throws IOException {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            FakeHttpExchange exchange = newExchangeFrom("10.0.0.1");
            handler.handle(exchange);
            assertEquals(HTTP_OK, exchange.responseCode());
        }
    }

    @Test
    void rejectsRequestsBeyondTheLimitWithRetryAfterHeader() throws IOException {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            handler.handle(newExchangeFrom("10.0.0.2"));
        }

        FakeHttpExchange exceedingRequest = newExchangeFrom("10.0.0.2");
        handler.handle(exceedingRequest);

        assertEquals(HTTP_TOO_MANY_REQUESTS, exceedingRequest.responseCode());
        assertTrue(exceedingRequest.responseHeaders().containsKey("Retry-After"));
    }

    @Test
    void tracksEachClientIndependently() throws IOException {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            handler.handle(newExchangeFrom("10.0.0.3"));
        }

        FakeHttpExchange otherClientRequest = newExchangeFrom("10.0.0.4");
        handler.handle(otherClientRequest);

        assertEquals(HTTP_OK, otherClientRequest.responseCode());
    }

    @Test
    void allowsRequestsAgainAfterTheWindowElapses() throws IOException {
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            handler.handle(newExchangeFrom("10.0.0.5"));
        }

        clock.advance(WINDOW.plusSeconds(1));
        FakeHttpExchange nextWindowRequest = newExchangeFrom("10.0.0.5");
        handler.handle(nextWindowRequest);

        assertEquals(HTTP_OK, nextWindowRequest.responseCode());
    }

    private void respondOk(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(HTTP_OK, 0);
    }

    private FakeHttpExchange newExchangeFrom(String ip) {
        return new FakeHttpExchange(new InetSocketAddress(ip, 0));
    }
}
