import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler appHandler = exchange -> {
            byte[] response = "OK".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        };

        // Allow 5 requests per minute per client, with bursting up to 5.
        RateLimitingHandler limited = new RateLimitingHandler(appHandler, 5);
        server.createContext("/api", limited);
        server.start();
    }
}
