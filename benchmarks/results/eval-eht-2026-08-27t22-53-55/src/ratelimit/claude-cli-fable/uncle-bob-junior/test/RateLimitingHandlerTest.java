import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimitingHandlerTest {

    private static final int MAX_REQUESTS = 2;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final int STATUS_OK = 200;
    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private static final int NO_RESPONSE_BODY = -1;

    private HttpServer server;
    private URI endpoint;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void startServer() throws IOException {
        SlidingWindowRateLimiter limiter =
                new SlidingWindowRateLimiter(MAX_REQUESTS, WINDOW, Clock.systemUTC());
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", new RateLimitingHandler(limiter,
                exchange -> exchange.sendResponseHeaders(STATUS_OK, NO_RESPONSE_BODY)));
        server.start();
        endpoint = URI.create("http://localhost:" + server.getAddress().getPort() + "/");
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void passesRequestsThroughUntilTheLimitThenReturns429WithRetryAfter() throws Exception {
        for (int request = 0; request < MAX_REQUESTS; request++) {
            assertEquals(STATUS_OK, send().statusCode());
        }
        HttpResponse<Void> rejected = send();
        assertEquals(STATUS_TOO_MANY_REQUESTS, rejected.statusCode());
        assertEquals(String.valueOf(WINDOW.toSeconds()),
                rejected.headers().firstValue("Retry-After").orElse(""));
    }

    private HttpResponse<Void> send() throws Exception {
        return client.send(HttpRequest.newBuilder(endpoint).GET().build(),
                HttpResponse.BodyHandlers.discarding());
    }
}
