import com.sun.net.httpserver.*;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(8080), 0);
        server.createContext("/", new RateLimitedHandler());
        server.start();
        System.out.println("Server running on port 8080");
    }
}
