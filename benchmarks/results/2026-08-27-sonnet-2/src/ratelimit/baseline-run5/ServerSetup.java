import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class ServerSetup {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api", new RateLimitingHandler(new MyApiHandler(), 5, 60_000L));
        server.start();
    }
}
