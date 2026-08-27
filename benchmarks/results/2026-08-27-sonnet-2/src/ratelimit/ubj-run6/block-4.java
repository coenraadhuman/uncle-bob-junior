// Usage example (not a separate file, just wiring):
//
// HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
// RateLimiter limiter = new FixedWindowRateLimiter(5, Duration.ofMinutes(1), Clock.systemUTC());
// server.createContext("/api", new RateLimitingHttpHandler(new MyApiHandler(), limiter, Duration.ofMinutes(1)));
// server.start();
