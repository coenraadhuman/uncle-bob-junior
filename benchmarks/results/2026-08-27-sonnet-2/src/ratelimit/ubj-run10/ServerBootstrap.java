import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

final class ServerBootstrap {
    static void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        RateLimitingHttpHandler handler = RateLimitingHttpHandler.withDefaults(exchange -> {
            byte[] body = "OK".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.getResponseBody().close();
        });
        server.createContext("/", handler);
        server.start();
    }
}
