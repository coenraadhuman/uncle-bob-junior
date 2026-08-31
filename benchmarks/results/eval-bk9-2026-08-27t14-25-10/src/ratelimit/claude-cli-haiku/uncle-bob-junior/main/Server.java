import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Server {
  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

    HttpHandler apiHandler = exchange -> {
      exchange.getResponseHeaders().set("Content-Type", "text/plain");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().write("OK".getBytes());
      exchange.close();
    };

    RateLimiter limiter = new RateLimiter();
    server.createContext("/api", new RateLimitedHandler(apiHandler, limiter));

    server.start();
    System.out.println("Server running on port 8080");
  }
}
