import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        HttpHandler helloHandler = exchange -> {
            byte[] body = "Hello\n".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.getResponseBody().close();
        };

        // 5 requests per minute per client IP
        server.createContext("/hello", RateLimitingHandler.perMinute(helloHandler, 5));
        server.start();
    }
}
