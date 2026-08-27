import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler myHandler = exchange -> {
            byte[] body = "Hello".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        };

        server.createContext("/api", new RateLimitingHandler(myHandler, 5, 60_000L));
        server.start();
    }
}
