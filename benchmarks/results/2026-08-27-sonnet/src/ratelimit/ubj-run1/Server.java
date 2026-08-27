package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;

/** Example wiring: at most 5 requests per minute per client IP on "/api". */
public final class Server {

    private static final int PORT = 8080;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(MAX_REQUESTS_PER_MINUTE, WINDOW);
        HttpHandlerAdapter apiHandler = new HttpHandlerAdapter();

        server.createContext("/api", new RateLimitingHandler(apiHandler, rateLimiter, WINDOW.toSeconds()));
        server.start();
    }

    private static final class HttpHandlerAdapter implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] body = "ok".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(body);
            }
        }
    }
}
