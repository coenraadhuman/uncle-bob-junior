public class DatabaseConfig {
    private int maxConnections = 10;
    private Duration connectionTimeout = Duration.ofSeconds(10);
    
    public void setMaxConnections(int value) {
        this.maxConnections = value;
    }
    
    public void setConnectionTimeout(Duration value) {
        this.connectionTimeout = value;
    }
    
    public int getMaxConnections() { return maxConnections; }
    public Duration getConnectionTimeout() { return connectionTimeout; }
}
