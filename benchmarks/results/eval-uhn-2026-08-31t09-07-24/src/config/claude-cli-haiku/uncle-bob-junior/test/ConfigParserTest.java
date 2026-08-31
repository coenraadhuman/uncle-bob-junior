class ConfigParserTest {
    
    static ConfigSchema schema() {
        ConfigSchema schema = new ConfigSchema();
        
        SectionSpec db = new SectionSpec("database");
        db.addKey(new KeySpec("port", KeyType.INT, new IntValue(5432)));
        db.addKey(new KeySpec("timeout", KeyType.DURATION, new DurationValue(30_000)));
        db.addKey(new KeySpec("debug", KeyType.BOOLEAN, new BooleanValue(false)));
        schema.addSection(db);
        
        SectionSpec app = new SectionSpec("app");
        app.addKey(new KeySpec("retries", KeyType.INT, new IntValue(3)));
        schema.addSection(app);
        
        return schema;
    }
    
    static void testParseInt() throws ValidationException {
        Configuration cfg = new ConfigParser(schema()).parse("[database]\nport = 9999");
        assert cfg.getInt("database", "port") == 9999;
    }
    
    static void testParseBoolean() throws ValidationException {
        Configuration cfg = new ConfigParser(schema()).parse("[database]\ndebug = true");
        assert cfg.getBoolean("database", "debug");
    }
    
    static void testParseDuration() throws ValidationException {
        Configuration cfg = new ConfigParser(schema()).parse("[database]\ntimeout = 2m");
        assert cfg.getDurationMillis("database", "timeout") == 120_000;
    }
    
    static void testDurationUnits() throws ValidationException {
        ConfigSchema s = schema();
        assert new ConfigParser(s).parse("[database]\ntimeout = 30s").getDurationMillis("database", "timeout") == 30_000;
        assert new ConfigParser(s).parse("[database]\ntimeout = 5m").getDurationMillis("database", "timeout") == 300_000;
        assert new ConfigParser(s).parse("[database]\ntimeout = 1h").getDurationMillis("database", "timeout") == 3_600_000;
    }
    
    static void testDefaults() throws ValidationException {
        Configuration cfg = new ConfigParser(schema()).parse("[database]\n[app]");
        assert cfg.getInt("database", "port") == 5432;
        assert cfg.getInt("app", "retries") == 3;
        assert !cfg.getBoolean("database", "debug");
    }
    
    static void testComments() throws ValidationException {
        Configuration cfg = new ConfigParser(schema()).parse("# header\n[database]\nport = 1234  # inline");
        assert cfg.getInt("database", "port") == 1234;
    }
    
    static void testMultipleSections() throws ValidationException {
        Configuration cfg = new ConfigParser(schema()).parse("[database]\nport = 1111\n[app]\nretries = 5");
        assert cfg.getInt("database", "port") == 1111;
        assert cfg.getInt("app", "retries") == 5;
    }
    
    static void testBlankLines() throws ValidationException {
        Configuration cfg = new ConfigParser(schema()).parse("\n[database]\n\nport = 8888\n");
        assert cfg.getInt("database", "port") == 8888;
    }
    
    static void testErrorUnknownSection() {
        try { new ConfigParser(schema()).parse("[unknown]"); assert false; } 
        catch (ValidationException e) { assert e.getMessage().contains("unknown section") && e.getMessage().contains("Line 1"); }
    }
    
    static void testErrorUnknownKey() {
        try { new ConfigParser(schema()).parse("[database]\nunknown = 5"); assert false; }
        catch (ValidationException e) { assert e.getMessage().contains("unknown key") && e.getMessage().contains("Line 2"); }
    }
    
    static void testErrorMalformed() {
        try { new ConfigParser(schema()).parse("[database]\nno_equals"); assert false; }
        catch (ValidationException e) { assert e.getMessage().contains("malformed") && e.getMessage().contains("Line 2"); }
    }
    
    static void testErrorInvalidInt() {
        try { new ConfigParser(schema()).parse("[database]\nport = abc"); assert false; }
        catch (ValidationException e) { assert e.getMessage().contains("invalid INT"); }
    }
    
    static void testErrorInvalidBoolean() {
        try { new ConfigParser(schema()).parse("[database]\ndebug = maybe"); assert false; }
        catch (ValidationException e) { assert e.getMessage().contains("invalid BOOLEAN"); }
    }
    
    static void testErrorInvalidDuration() {
        try { new ConfigParser(schema()).parse("[database]\ntimeout = 30x"); assert false; }
        catch (ValidationException e) { assert e.getMessage().contains("invalid DURATION"); }
    }
    
    static void testErrorOutsideSection() {
        try { new ConfigParser(schema()).parse("port = 5432"); assert false; }
        catch (ValidationException e) { assert e.getMessage().contains("outside any section") && e.getMessage().contains("Line 1"); }
    }
    
    public static void main(String[] args) {
        try {
            testParseInt();
            testParseBoolean();
            testParseDuration();
            testDurationUnits();
            testDefaults();
            testComments();
            testMultipleSections();
            testBlankLines();
            System.out.println("✓ 8 success cases");
        } catch (Exception e) { System.err.println("✗ " + e); e.printStackTrace(); }
        
        try {
            testErrorUnknownSection();
            testErrorUnknownKey();
            testErrorMalformed();
            testErrorInvalidInt();
            testErrorInvalidBoolean();
            testErrorInvalidDuration();
            testErrorOutsideSection();
            System.out.println("✓ 7 error cases");
        } catch (AssertionError e) { System.err.println("✗ " + e); e.printStackTrace(); }
    }
}
