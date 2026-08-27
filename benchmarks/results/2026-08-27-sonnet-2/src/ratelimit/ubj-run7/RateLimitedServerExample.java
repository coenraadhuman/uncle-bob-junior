package com.plg.ratelimit;

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
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        RateLimiter rateLimiter =
                new SlidingWindowRateLimiter(MAX_REQUESTS_PER_MINUTE, WINDOW, Clock.systemUTC());

        server.createContext("/",
                new RateLimitingHttpHandler(RateLimitedServerExample::handleRequest, rateLimiter, WINDOW));

        server.start();
    }

    private static void handleRequest(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        byte[] body = "OK".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
