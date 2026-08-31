class RateLimitedHandler {
  private final RateLimiter rateLimiter;

  RateLimitedHandler(RateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
  }

  HttpResponse handle(String clientIp, String requestBody) {
    if (!rateLimiter.isAllowed(clientIp)) {
      return new HttpResponse(429, "Too Many Requests");
    }
    return new HttpResponse(200, "OK");
  }
}
