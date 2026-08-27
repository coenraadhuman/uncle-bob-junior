// For servlet filter, register in web.xml or with annotation:
@WebFilter("/*")
public class RateLimitingFilter implements Filter { ... }

// For HttpServer:
server.createContext("/api", 
    new RateLimitedHandler(new YourApiHandler()));
