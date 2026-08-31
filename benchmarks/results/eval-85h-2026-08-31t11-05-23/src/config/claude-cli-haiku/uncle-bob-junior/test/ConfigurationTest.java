class ConfigurationTest {
    @Test void parsesValidConfiguration() {
        String input = "timeout=60\ndebug=true\nretry_delay=5s";
        Configuration config = Configuration.parse(input);
        assertEquals(60, config.getTimeout());
        assertEquals(true, config.isDebug());
        assertEquals(5000, config.getRetryDelayMs());
        assertEquals(0, config.getErrors().size());
    }
    
    @Test void usesDefaults() {
        Configuration config = Configuration.parse("");
        assertEquals(30, config.getTimeout());
        assertEquals(false, config.isDebug());
        assertEquals(5000, config.getRetryDelayMs());
    }
    
    @Test void skipsCommentsAndBlankLines() {
        String input = "# comment\ntimeout=60\n\n# another\n";
        Configuration config = Configuration.parse(input);
        assertEquals(60, config.getTimeout());
        assertEquals(0, config.getErrors().size());
    }
    
    @Test void reportsMalformedLineWithNumber() {
        String input = "timeout 60";
        Configuration config = Configuration.parse(input);
        assertEquals(1, config.getErrors().size());
        assertEquals(1, config.getErrors().get(0).lineNumber());
        assertEquals("Malformed line", config.getErrors().get(0).message());
    }
    
    @Test void reportsUnknownKeyWithNumber() {
        String input = "timeout=60\nunknown_key=value";
        Configuration config = Configuration.parse(input);
        assertEquals(1, config.getErrors().size());
        assertEquals(2, config.getErrors().get(0).lineNumber());
        assertEquals("Unknown key: unknown_key", config.getErrors().get(0).message());
    }
    
    @Test void reportsInvalidInteger() {
        String input = "timeout=abc";
        Configuration config = Configuration.parse(input);
        assertEquals(1, config.getErrors().size());
        assertEquals("Invalid integer: abc", config.getErrors().get(0).message());
    }
    
    @Test void reportsInvalidBoolean() {
        String input = "debug=maybe";
        Configuration config = Configuration.parse(input);
        assertEquals(1, config.getErrors().size());
        assertEquals("Invalid boolean: maybe", config.getErrors().get(0).message());
    }
    
    @Test void parsesDurationWithVariousUnits() {
        assertEquals(30_000, Configuration.parse("retry_delay=30s").getRetryDelayMs());
        assertEquals(2_000, Configuration.parse("retry_delay=2s").getRetryDelayMs());
        assertEquals(120_000, Configuration.parse("retry_delay=2m").getRetryDelayMs());
        assertEquals(500, Configuration.parse("retry_delay=500ms").getRetryDelayMs());
        assertEquals(3_600_000, Configuration.parse("retry_delay=1h").getRetryDelayMs());
    }
    
    @Test void reportsInvalidDurationUnit() {
        String input = "retry_delay=10x";
        Configuration config = Configuration.parse(input);
        assertEquals(1, config.getErrors().size());
        assertEquals("Unknown duration unit: x", config.getErrors().get(0).message());
    }
    
    @Test void ignoresSections() {
        String input = "[section1]\ntimeout=60\n[section2]\ndebug=true";
        Configuration config = Configuration.parse(input);
        assertEquals(60, config.getTimeout());
        assertEquals(true, config.isDebug());
        assertEquals(0, config.getErrors().size());
    }
    
    @Test void handlesWhitespace() {
        String input = "  timeout  =  42  \n  debug = true  ";
        Configuration config = Configuration.parse(input);
        assertEquals(42, config.getTimeout());
        assertEquals(true, config.isDebug());
    }
    
    @Test void acceptsBooleanVariants() {
        assertEquals(true, Configuration.parse("debug=yes").isDebug());
        assertEquals(true, Configuration.parse("debug=on").isDebug());
        assertEquals(true, Configuration.parse("debug=1").isDebug());
        assertEquals(false, Configuration.parse("debug=no").isDebug());
        assertEquals(false, Configuration.parse("debug=off").isDebug());
    }
}
