package ratelimit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitingHttpHandlerTest {

    private static final int MAX_REQUESTS_PER_WINDOW = 3;
    private static final int HTTP_OK = 200;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int NO_RESPONSE_BODY = -1;

    @Test
    void allowsRequestsUpToLimitThenRejectsFurtherRequests() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        RateLimiter rateLimiter = new RateLimiter(MAX_REQUESTS_PER_WINDOW, Duration.ofMinutes(1));
        HttpHandler okHandler = RateLimitingHttpHandlerTest::respondOk;
        server.createContext("/", new RateLimitingHttpHandler(okHandler, rateLimiter, Duration.ofMinutes(1)));
        server.start();

        try {
            HttpClient client = HttpClient.newHttpClient();
            URI uri = URI.create("http://localhost:" + server.getAddress().getPort() + "/");

            for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
                assertEquals(HTTP_OK, sendGet(client, uri));
            }
            assertEquals(HTTP_TOO_MANY_REQUESTS, sendGet(client, uri));
        } finally {
            server.stop(0);
        }
    }

    private static int sendGet(HttpClient client, URI uri) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        return response.statusCode();
    }

    private static void respondOk(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(HTTP_OK, NO_RESPONSE_BODY);
        exchange.close();
    }
}
