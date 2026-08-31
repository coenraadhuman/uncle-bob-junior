public class Main {
    public static void main(String[] args) {
        String configText = """
            # Server configuration
            [server]
            port=9000
            debug=true
            timeout=2m
            
            # Connection settings
            [connection]
            maxConnections=50
            ssl=yes
            
            # Missing keys use defaults
            # badKey=value
            """;
        
        ConfigParser parser = new ConfigParser()
            .define("port", int.class, 8080)
            .define("debug", boolean.class, false)
            .define("timeout", Duration.class, Duration.ofSeconds(30))
            .define("maxConnections", int.class, 100)
            .define("ssl", boolean.class, false);
        
        AppConfig config = new AppConfig();
        ParseResult<AppConfig> result = parser.parse(configText, config);
        
        System.out.println(result.config);
        if (result.hasErrors()) {
            System.out.println("\nValidation errors:");
            result.errors.forEach(System.out::println);
        }
    }
}
