public class ServerConfig {
    private int port = 8080;
    private Duration timeout = Duration.ofSeconds(30);
    private boolean debug = false;
    
    public void setPort(int value) {
        this.port = value;
    }
    
    public void setTimeout(Duration value) {
        this.timeout = value;
    }
    
    public void setDebug(boolean value) {
        this.debug = value;
    }
    
    public int getPort() { return port; }
    public Duration getTimeout() { return timeout; }
    public boolean isDebug() { return debug; }
}
