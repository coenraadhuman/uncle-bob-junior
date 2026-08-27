import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.*;

public class RateLimitedHandler implements HttpHandler {
  private final HttpHandler delegate;
  private final RateLimiter rateLimiter;

  public RateLimitedHandler(HttpHandler delegate, RateLimiter rateLimiter) {
    this.delegate = delegate;
    this.rateLimiter = rateLimiter;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String clientIp = getClientIp(exchange);

    if (!rateLimiter.allowRequest(clientIp)) {
      exchange.getResponseHeaders().set("Retry-After", "60");
      exchange.sendResponseHeaders(429, 0);
      exchange.close();
      return;
    }

    delegate.handle(exchange);
  }

  private String getClientIp(HttpExchange exchange) {
    return exchange.getRemoteAddress().getAddress().getHostAddress();
  }
}
