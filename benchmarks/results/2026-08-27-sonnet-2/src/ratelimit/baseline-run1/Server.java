import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler helloHandler = exchange -> {
            byte[] body = "Hello, world!\n".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        };

        // Limit to 5 requests per 60 seconds per client IP.
        server.createContext("/hello", new RateLimitingHandler(helloHandler, 5, 60_000L));

        server.start();
    }
}
