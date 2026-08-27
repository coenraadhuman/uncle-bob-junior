package com.example.ratelimit;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * Example wiring: 5 requests per 60 seconds per client IP.
 */
public final class ExampleServer {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1));

        HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
        server.createContext("/", new RateLimitingHttpHandler(ExampleServer::handleRequest, rateLimiter));
        server.start();
    }

    private static void handleRequest(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
        byte[] responseBody = "OK\n".getBytes();
        exchange.sendResponseHeaders(200, responseBody.length);
        try (var responseStream = exchange.getResponseBody()) {
            responseStream.write(responseBody);
        }
    }
}
