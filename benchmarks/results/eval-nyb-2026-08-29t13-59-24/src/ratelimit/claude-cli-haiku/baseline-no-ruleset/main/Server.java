import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/api", new RateLimitingHandler(
            exchange -> {
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().write("Hello World".getBytes());
                exchange.close();
            },
            5,           // max 5 requests
            60000        // per 60 seconds
        ));
        
        server.start();
        System.out.println("Server started on port 8080");
    }
}
