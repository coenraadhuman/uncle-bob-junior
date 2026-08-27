HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
server.createContext("/api", new RateLimitingHandler(exchange -> {
    // actual request handling
    String response = "OK";
    exchange.sendResponseHeaders(200, response.length());
    exchange.getResponseBody().write(response.getBytes());
    exchange.getResponseBody().close();
}));
server.start();
