import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class RateLimitedServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        RateLimiter rateLimiter = new RateLimiter(10);
        HttpHandler apiHandler = new ExampleApiHandler();
        HttpHandler rateLimitedHandler = new RateLimitedHttpHandler(apiHandler, rateLimiter);

        server.createContext("/api", rateLimitedHandler);
        server.start();
        System.out.println("Server started on http://localhost:8080");
    }
}
