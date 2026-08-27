import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        ClientRateLimiter rateLimiter = new ClientRateLimiter();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/api", new RateLimitedHttpHandler(
            exchange -> {
                byte[] response = "Hello!".getBytes();
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            },
            rateLimiter
        ));
        
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            rateLimiter.shutdown();
            server.stop(0);
        }));
    }
}
