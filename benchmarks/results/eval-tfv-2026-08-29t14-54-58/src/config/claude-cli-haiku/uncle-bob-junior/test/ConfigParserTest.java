public final class ConfigParserTest {
    
    public static void main(String[] args) throws Exception {
        testBasicParsing();
        testComments();
        testSections();
        testDefaults();
        testValidationErrors();
        testDurationParsing();
        testBooleanCaseInsensitive();
        testEmptyConfig();
        testMissingRequiredKey();
        System.out.println("All tests passed!");
    }
    
    private static void testBasicParsing() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "port", new ConfigParser.KeyDef("port", "int", 8080),
            "timeout", new ConfigParser.KeyDef("timeout", "duration", null),
            "debug", new ConfigParser.KeyDef("debug", "boolean", false)
        );
        
        ConfigParser.Parser parser = new ConfigParser.Parser(schema);
        ConfigParser.Config cfg = parser.parse("port=9000\ndebug=true\ntimeout=30s");
        
        assert cfg.getInt("port") == 9000;
        assert cfg.getBoolean("debug");
        assert cfg.getDuration("timeout").toMillis() == 30000;
    }
    
    private static void testComments() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "key", new ConfigParser.KeyDef("key", "int", 0)
        );
        
        ConfigParser.Parser parser = new ConfigParser.Parser(schema);
        ConfigParser.Config cfg = parser.parse("# comment\nkey=100\n\n# another");
        assert cfg.getInt("key") == 100;
    }
    
    private static void testSections() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "timeout", new ConfigParser.KeyDef("timeout", "duration", null),
            "retries", new ConfigParser.KeyDef("retries", "int", 0)
        );
        
        ConfigParser.Parser parser = new ConfigParser.Parser(schema);
        ConfigParser.Config cfg = parser.parse("[server]\ntimeout=30s\n[client]\nretries=5");
        
        assert cfg.getDuration("server", "timeout").toMillis() == 30000;
        assert cfg.getInt("client", "retries") == 5;
    }
    
    private static void testDefaults() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "port", new ConfigParser.KeyDef("port", "int", 8080),
            "timeout", new ConfigParser.KeyDef("timeout", "duration", new ConfigParser.Duration(60000))
        );
        
        ConfigParser.Parser parser = new ConfigParser.Parser(schema);
        ConfigParser.Config cfg = parser.parse("");
        
        assert cfg.getInt("port") == 8080;
        assert cfg.getDuration("timeout").toMillis() == 60000;
    }
    
    private static void testValidationErrors() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "port", new ConfigParser.KeyDef("port", "int", 0)
        );
        
        ConfigParser.Parser parser = new ConfigParser.Parser(schema);
        try {
            parser.parse("port=abc\nunknown=val\nbad line");
            assert false;
        } catch (ConfigParser.ValidationException e) {
            List<String> errors = e.getErrors();
            assert errors.size() >= 2;
            assert errors.stream().anyMatch(e2 -> e2.contains("Line 1") && e2.contains("Invalid integer"));
            assert errors.stream().anyMatch(e2 -> e2.contains("Unknown key: unknown"));
            assert errors.stream().anyMatch(e2 -> e2.contains("Line 3") && e2.contains("Malformed"));
        }
    }
    
    private static void testDurationParsing() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "d", new ConfigParser.KeyDef("d", "duration", null)
        );
        
        ConfigParser.Parser parser = new ConfigParser.Parser(schema);
        
        assert parser.parse("d=5s").getDuration("d").toMillis() == 5000;
        assert parser.parse("d=2m").getDuration("d").toMillis() == 120000;
        assert parser.parse("d=1h").getDuration("d").toMillis() == 3600000;
    }
    
    private static void testBooleanCaseInsensitive() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "b", new ConfigParser.KeyDef("b", "boolean", false)
        );
        
        ConfigParser.Parser parser = new ConfigParser.Parser(schema);
        assert parser.parse("b=true").getBoolean("b");
        assert parser.parse("b=TRUE").getBoolean("b");
        assert parser.parse("b=True").getBoolean("b");
        assert !parser.parse("b=false").getBoolean("b");
    }
    
    private static void testEmptyConfig() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "opt", new ConfigParser.KeyDef("opt", "int", 42)
        );
        
        ConfigParser.Config cfg = new ConfigParser.Parser(schema).parse("\n\n");
        assert cfg.getInt("opt") == 42;
    }
    
    private static void testMissingRequiredKey() throws Exception {
        Map<String, ConfigParser.KeyDef> schema = Map.of(
            "required", new ConfigParser.KeyDef("required", "int", null)
        );
        
        ConfigParser.Config cfg = new ConfigParser.Parser(schema).parse("");
        try {
            cfg.getInt("required");
            assert false;
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("not found");
        }
    }
}
