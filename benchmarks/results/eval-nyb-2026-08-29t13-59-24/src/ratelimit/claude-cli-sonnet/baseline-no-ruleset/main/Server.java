import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        HttpHandler apiHandler = exchange -> {
            byte[] response = "Hello".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        };

        server.createContext("/api", RateLimitingHandler.perMinute(apiHandler, 5));
        server.start();
    }
}
