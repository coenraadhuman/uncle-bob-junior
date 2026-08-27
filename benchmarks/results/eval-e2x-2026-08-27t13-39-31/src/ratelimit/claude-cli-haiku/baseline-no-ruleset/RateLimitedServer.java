import com.sun.net.httpserver.*;
import java.net.InetSocketAddress;

public class RateLimitedServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new RateLimitedHttpHandler());
        server.setExecutor(null); // Default executor
        server.start();
        System.out.println("Server started on port 8080");
        System.out.println("Rate limit: 5 requests per minute per client");
    }
}
