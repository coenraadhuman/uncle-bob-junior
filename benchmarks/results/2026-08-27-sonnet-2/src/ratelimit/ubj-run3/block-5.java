RateLimiter rateLimiter = new RateLimiter(5, Duration.ofMinutes(1));
server.createContext("/api", new RateLimitedHttpHandler(actualHandler, rateLimiter));
