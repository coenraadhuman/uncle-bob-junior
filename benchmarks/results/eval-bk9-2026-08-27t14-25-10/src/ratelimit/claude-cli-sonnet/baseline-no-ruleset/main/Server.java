import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public final class Server {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandlerImpl actualHandler = new HttpHandlerImpl();
        RateLimitingHandler limited = new RateLimitingHandler(actualHandler, 5, 60_000L);

        server.createContext("/api", limited);
        server.setExecutor(null);
        server.start();
    }

    static final class HttpHandlerImpl implements com.sun.net.httpserver.HttpHandler {
        @Override
        public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            byte[] body = "OK\n".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        }
    }
}
