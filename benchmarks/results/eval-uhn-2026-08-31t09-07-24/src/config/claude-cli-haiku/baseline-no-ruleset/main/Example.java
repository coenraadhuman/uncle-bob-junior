import java.util.*;

// Example usage
public class Example {
    public static void main(String[] args) {
        String configText = """
                # Application configuration
                [server]
                port=8080
                timeout=30s
                debug=true
                
                # Cache settings
                [cache]
                ttl=5m
                enabled=false
                max_size=10000
                
                [cache]
                enabled=true
                """;

        Map<String, Map<String, Object>> defaults = new HashMap<>();
        defaults.put("server", Map.of("port", 8080, "timeout", 30, "debug", false));
        defaults.put("cache", Map.of("ttl", 300, "enabled", false, "max_size", 5000));

        ConfigParser parser = new ConfigParser(configText, defaults);
        Config config = parser.parse();

        if (!config.isValid()) {
            System.out.println("Validation errors:");
            config.getErrors().forEach(System.err::println);
        }

        System.out.println("\nConfiguration:");
        System.out.println("Server port: " + config.getInt("server", "port"));
        System.out.println("Server timeout: " + config.getInt("server", "timeout") + "s");
        System.out.println("Server debug: " + config.getBoolean("server", "debug"));
        System.out.println("Cache TTL: " + config.getInt("cache", "ttl") + "s");
        System.out.println("Cache enabled: " + config.getBoolean("cache", "enabled"));
        System.out.println("Cache max size: " + config.getInt("cache", "max_size"));
    }
}
