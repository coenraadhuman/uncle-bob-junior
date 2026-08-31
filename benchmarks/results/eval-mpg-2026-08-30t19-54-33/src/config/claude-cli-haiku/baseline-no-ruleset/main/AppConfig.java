public class AppConfig {
    public int port = 0;
    public boolean debug = false;
    public Duration timeout = null;
    public int maxConnections = 0;
    public boolean ssl = false;
    
    @Override
    public String toString() {
        return "AppConfig{port=" + port + ", debug=" + debug + ", timeout=" + timeout +
               ", maxConnections=" + maxConnections + ", ssl=" + ssl + '}';
    }
}
