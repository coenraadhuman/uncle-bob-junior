// File: Main.java  (example wiring, not a class under test)
package com.postcodeloterij.ratelimit;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

public final class Main {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        TokenBucketRateLimiter rateLimiter =
                new TokenBucketRateLimiter(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1));

        server.createContext("/api", new RateLimitingHandler(
                exchange -> exchange.sendResponseHeaders(200, -1),
                rateLimiter));
        server.start();
    }
}
