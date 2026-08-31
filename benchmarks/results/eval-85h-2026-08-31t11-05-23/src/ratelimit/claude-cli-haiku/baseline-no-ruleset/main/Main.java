import com.sun.net.httpserver.*;

// Example: Start HTTP server with rate limiting
public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(8080), 0);
        server.createContext("/", new RateLimitedHttpHandler());
        server.start();
        System.out.println("Server running on port 8080 (10 requests/minute per client)");
    }
}
