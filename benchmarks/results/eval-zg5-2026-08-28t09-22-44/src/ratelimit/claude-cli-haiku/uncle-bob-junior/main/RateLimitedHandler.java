class RateLimitedHandler implements HttpHandler {
  private final RateLimiter limiter;
  
  RateLimitedHandler(RateLimiter limiter) {
    this.limiter = limiter;
  }
  
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String clientId = extractClientIp(exchange);
    
    if (!limiter.allowRequest(clientId)) {
      sendTooManyRequests(exchange);
      return;
    }
    
    handleRequest(exchange);
  }
  
  private String extractClientIp(HttpExchange exchange) {
    InetSocketAddress remote = exchange.getRemoteAddress();
    if (remote == null) {
      return "unknown";
    }
    return remote.getAddress().getHostAddress();
  }
  
  private void sendTooManyRequests(HttpExchange exchange) throws IOException {
    exchange.getResponseHeaders().set("Retry-After", "60");
    exchange.sendResponseHeaders(429, 0);
    exchange.getResponseBody().close();
  }
  
  private void handleRequest(HttpExchange exchange) throws IOException {
    byte[] response = "OK".getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(200, response.length);
    exchange.getResponseBody().write(response);
    exchange.getResponseBody().close();
  }
}
