public class ServerConfig {
    private final int port;
    private final boolean ssl;
    private final Duration timeout;
    
    public ServerConfig(int port, boolean ssl, Duration timeout) {
        this.port = port;
        this.ssl = ssl;
        this.timeout = timeout;
    }
    
    public int port() { return port; }
    public boolean ssl() { return ssl; }
    public Duration timeout() { return timeout; }
}
