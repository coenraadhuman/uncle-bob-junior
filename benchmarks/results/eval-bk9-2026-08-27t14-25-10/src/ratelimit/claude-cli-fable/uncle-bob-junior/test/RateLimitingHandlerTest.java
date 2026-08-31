package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** All requests come from 127.0.0.1, so they share one client's limit. */
class RateLimitingHandlerTest {

    private static final int MAX_REQUESTS = 2;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final int STATUS_OK = 200;
    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private static final String DELEGATE_BODY = "hello";

    private HttpServer server;
    private HttpClient client;

    @BeforeEach
    void startServer() throws IOException {
        SlidingWindowRateLimiter limiter =
                new SlidingWindowRateLimiter(MAX_REQUESTS, WINDOW, Clock.systemUTC());
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", new RateLimitingHandler(limiter, RateLimitingHandlerTest::okDelegate));
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void passesRequestsUnderTheLimitToTheDelegate() throws Exception {
        for (int request = 0; request < MAX_REQUESTS; request++) {
            HttpResponse<String> response = get();
            assertEquals(STATUS_OK, response.statusCode());
            assertEquals(DELEGATE_BODY, response.body());
        }
    }

    @Test
    void rejectsRequestsOverTheLimitWith429AndRetryAfter() throws Exception {
        for (int request = 0; request < MAX_REQUESTS; request++) {
            get();
        }

        HttpResponse<String> rejected = get();

        assertEquals(STATUS_TOO_MANY_REQUESTS, rejected.statusCode());
        long retryAfterSeconds = Long.parseLong(
                rejected.headers().firstValue("Retry-After").orElseThrow());
        assertTrue(retryAfterSeconds >= 1 && retryAfterSeconds <= WINDOW.toSeconds());
    }

    private HttpResponse<String> get() throws IOException, InterruptedException {
        URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/");
        return client.send(HttpRequest.newBuilder(uri).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
