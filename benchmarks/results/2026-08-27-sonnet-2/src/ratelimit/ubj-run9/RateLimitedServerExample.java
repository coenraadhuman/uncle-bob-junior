// File: RateLimitedServerExample.java
package com.example.ratelimit;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

public final class RateLimitedServerExample {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        FixedWindowRateLimiter rateLimiter =
                new FixedWindowRateLimiter(MAX_REQUESTS_PER_MINUTE, RATE_LIMIT_WINDOW, Clock.systemUTC());

        server.createContext("/", new RateLimitingHttpHandler(helloHandler(), rateLimiter, RATE_LIMIT_WINDOW));
        server.start();
    }

    private static HttpHandler helloHandler() {
        return exchange -> {
            byte[] response = "Hello, world!".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(response);
            }
        };
    }
}
