// RateLimitedServerExample.java
package com.example.ratelimit;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;

public final class RateLimitedServerExample {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public static void main(String[] args) throws IOException {
        RateLimitConfig config = new RateLimitConfig(MAX_REQUESTS_PER_WINDOW, WINDOW);
        RateLimiter rateLimiter = new SlidingWindowRateLimiter(config);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RateLimitingHandler(
                exchange -> exchange.sendResponseHeaders(200, -1),
                rateLimiter,
                Clock.systemUTC(),
                WINDOW));
        server.start();
    }
}
