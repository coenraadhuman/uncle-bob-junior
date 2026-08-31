public class ConfigParserTest {
    public static void main(String[] args) {
        testDurationSeconds();
        testDurationMinutes();
        testDurationHours();
        testDurationInvalid();
        testParseSimpleConfig();
        testParseWithDefaults();
        testUnknownSection();
        testUnknownKey();
        testInvalidIntValue();
        testInvalidBoolValue();
        testInvalidDurationValue();
        testMalformedLine();
        testCommentsAndBlanks();
        testMultipleSections();
        testMissingRequiredKey();
        testKeyOutsideSection();
        testMultipleErrors();
        System.out.println("All tests passed");
    }
    
    static void testDurationSeconds() {
        Duration d = Duration.parse("30s");
        assert d.toMillis() == 30000;
    }
    
    static void testDurationMinutes() {
        Duration d = Duration.parse("5m");
        assert d.toMillis() == 300000;
    }
    
    static void testDurationHours() {
        Duration d = Duration.parse("1h");
        assert d.toMillis() == 3600000;
    }
    
    static void testDurationInvalid() {
        try {
            Duration.parse("invalid");
            assert false;
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Invalid duration format");
        }
    }
    
    static void testParseSimpleConfig() throws ConfigValidationException {
        String content = "[server]\nport=8080\ndebug=true\ntimeout=30s\nname=localhost";
        
        ConfigSchema schema = new ConfigSchema()
            .addSection("server")
            .addInt("server", "port", 0)
            .addBoolean("server", "debug", false)
            .addDuration("server", "timeout", Duration.of(0))
            .addString("server", "name", "");
        
        Configuration cfg = ConfigParser.parse(content, schema);
        assert cfg.getInt("server", "port") == 8080;
        assert cfg.getBoolean("server", "debug") == true;
        assert cfg.getDuration("server", "timeout").toMillis() == 30000;
        assert "localhost".equals(cfg.getString("server", "name"));
    }
    
    static void testParseWithDefaults() throws ConfigValidationException {
        String content = "[server]\nport=9000";
        
        ConfigSchema schema = new ConfigSchema()
            .addSection("server")
            .addInt("server", "port", 8080)
            .addBoolean("server", "debug", false);
        
        Configuration cfg = ConfigParser.parse(content, schema);
        assert cfg.getInt("server", "port") == 9000;
        assert cfg.getBoolean("server", "debug") == false;
    }
    
    static void testUnknownSection() {
        String content = "[unknown]\nkey=value";
        ConfigSchema schema = new ConfigSchema().addSection("server");
        
        try {
            ConfigParser.parse(content, schema);
            assert false;
        } catch (ConfigValidationException e) {
            assert e.getErrors().stream().anyMatch(err -> err.contains("Unknown section"));
        }
    }
    
    static void testUnknownKey() {
        String content = "[server]\nunknown=value";
        ConfigSchema schema = new ConfigSchema()
            .addSection("server")
            .addString("server", "name", "");
        
        try {
            ConfigParser.parse(content, schema);
            assert false;
        } catch (ConfigValidationException e) {
            assert e.getErrors().stream().anyMatch(err -> err.contains("Unknown key"));
        }
    }
    
    static void testInvalidIntValue() {
        String content = "[server]\nport=notanumber";
        ConfigSchema schema = new ConfigSchema()
            .addSection("server")
            .addInt("server", "port", 0);
        
        try {
            ConfigParser.parse(content, schema);
            assert false;
        } catch (ConfigValidationException e) {
            assert e.getErrors().stream().anyMatch(err -> err.contains("Invalid value"));
        }
    }
    
    static void testInvalidBoolValue() {
        String content = "[server]\ndebug=maybe";
        ConfigSchema schema = new ConfigSchema()
            .addSection("server")
            .addBoolean("server", "debug", false);
        
        try {
            ConfigParser.parse(content, schema);
            assert false;
        } catch (ConfigValidationException e) {
            assert e.getErrors().stream().anyMatch(err -> err.contains("Invalid value"));
        }
    }
    
    static void testInvalidDurationValue() {
        String content = "[server]\ntimeout=invalid";
        ConfigSchema schema = new ConfigSchema()
            .addSection("server")
            .addDuration("server", "timeout", Duration.of(0));
        
        try {
            ConfigParser.parse(content, schema);
            assert false;
        } catch (ConfigValidationException e) {
            assert e.getErrors().stream().anyMatch(err -> err.contains("Invalid value"));
        }
    }
    
    static void testMalformedLine() {
        String content = "[server]\nbad line";
        ConfigSchema schema = new ConfigSchema().addSection("server");
        
        try {
            ConfigParser.parse(content, schema);
            assert false;
        } catch (ConfigValidationException e) {
            assert e.getErrors().stream().anyMatch(err -> err.contains("Malformed line"));
        }
    }
    
    static void testCommentsAndBlanks() throws ConfigValidationException {
        String content = "# comment\n\n[server]\n# another\nport=8080\n\n# third\ndebug=true";
        
        ConfigSchema schema = new ConfigSchema()
            .addSection("server")
            .addInt("server", "port", 0)
            .addBoolean("server", "debug", false);
        
        Configuration cfg = ConfigParser.parse(content, schema);
        assert cfg.getInt("server", "port") == 8080;
        assert cfg.getBoolean("server", "debug") == true;
    }
    
    static void testMultipleSections() throws ConfigValidationException {
        String content = "[server]\nport=8080\n\n[logging]\nlevel=debug\nmaxSize=1000";
        
        ConfigSchema schema = new ConfigSchema()
            .addSection("server").addInt("server", "port", 0)
            .addSection("logging")
            .addString("logging", "level", "info")
            .addInt("logging", "maxSize", 100);
        
        Configuration cfg = ConfigParser.parse(content, schema);
        assert cfg.getInt("server", "port") == 8080;
        assert "debug".equals(cfg.getString("logging", "level"));
        assert cfg.getInt("logging", "maxSize") == 1000;
    }
    
    static void testMissingRequiredKey() {
        String content = "[server]";
        ConfigSchema schema = new ConfigSchema()
            .addSection("server")
            .addIntRequired("server", "port");
        
        try {
            ConfigParser.parse(content, schema);
            assert false;
        } catch (ConfigValidationException e) {
            assert e.getErrors().stream().anyMatch(err -> err.contains("Missing required key"));
        }
    }
    
    static void testKeyOutsideSection() {
        String content = "key=value";
        ConfigSchema schema = new ConfigSchema().addSection("server");
        
        try {
            ConfigParser.parse(content, schema);
            assert false;
        } catch (ConfigValidationException e) {
            assert e.getErrors().stream().anyMatch(err -> err.contains("outside of section"));
        }
    }
    
    static void testMultipleErrors() {
        String content = "[unknown]\nport=bad\n[server]\nkey=";
        ConfigSchema schema = new ConfigSchema()
            .addSection("server")
            .addInt("server", "key", 0);
        
        try {
            ConfigParser.parse(content, schema);
            assert false;
        } catch (ConfigValidationException e) {
            assert e.getErrors().size() >= 2;
        }
    }
}
