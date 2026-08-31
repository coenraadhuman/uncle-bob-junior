package com.example.ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

public final class RateLimitedServerExample {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(
                MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1), Clock.systemUTC());

        HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
        server.createContext("/", new RateLimitingHttpHandler(
                RateLimitedServerExample::handleHello, rateLimiter));
        server.start();
    }

    private static void handleHello(HttpExchange exchange) throws IOException {
        byte[] body = "Hello, world!".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }
}
