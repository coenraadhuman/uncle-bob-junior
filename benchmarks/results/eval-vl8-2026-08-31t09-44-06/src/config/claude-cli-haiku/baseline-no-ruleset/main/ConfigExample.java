public class ConfigExample {
    public static void main(String[] args) {
        String config = """
            [server]
            port = 8080
            timeout = 30s
            debug = true
            
            [database]
            maxConnections = 50
            connectionTimeout = 5m
            enabled = false
            """;
        
        ConfigParser parser = new ConfigParser()
            .addValidKey("server", "port")
            .addValidKey("server", "timeout")
            .addValidKey("server", "debug")
            .addValidKey("database", "maxConnections")
            .addValidKey("database", "connectionTimeout")
            .addValidKey("database", "enabled")
            .addDefaults("server", "port", 3000)
            .addDefaults("server", "debug", false);
        
        try {
            Configuration cfg = parser.parse(config);
            System.out.println("port: " + cfg.getInt("server", "port"));
            System.out.println("timeout: " + cfg.getDuration("server", "timeout").toSeconds() + "s");
            System.out.println("debug: " + cfg.getBoolean("server", "debug"));
            System.out.println("maxConnections: " + cfg.getInt("database", "maxConnections"));
        } catch (ConfigException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
