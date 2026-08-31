class ConfigParserTest {
    
    @Test void defaultsWhenEmpty() {
        ParseResult<ServerConfig> result = new ConfigParser("[server]").parse();
        assertTrue(result.ok());
        assertEquals(8080, result.value().port());
        assertFalse(result.value().ssl());
        assertEquals(30_000, result.value().timeout().milliseconds());
    }
    
    @Test void parsesAllTypes() {
        String config = "port=9000\nssl=true\ntimeout=5m";
        ParseResult<ServerConfig> result = new ConfigParser(config).parse();
        assertTrue(result.ok());
        assertEquals(9000, result.value().port());
        assertTrue(result.value().ssl());
        assertEquals(300_000, result.value().timeout().milliseconds());
    }
    
    @Test void ignoresCommentsAndBlankLines() {
        String config = "\n# comment\nport=8000 # inline\n\n";
        ParseResult<ServerConfig> result = new ConfigParser(config).parse();
        assertTrue(result.ok());
        assertEquals(8000, result.value().port());
    }
    
    @Test void malformedLineReportsLineNumber() {
        ParseResult<ServerConfig> result = new ConfigParser("port:8000").parse();
        assertFalse(result.ok());
        assertEquals(1, result.errors().get(0).line());
        assertTrue(result.errors().get(0).message().contains("Malformed"));
    }
    
    @Test void unknownKeyReportsLineNumber() {
        ParseResult<ServerConfig> result = new ConfigParser("foo=bar").parse();
        assertFalse(result.ok());
        assertEquals(1, result.errors().get(0).line());
        assertTrue(result.errors().get(0).message().contains("Unknown key: foo"));
    }
    
    @Test void invalidIntegerReportsError() {
        ParseResult<ServerConfig> result = new ConfigParser("port=abc").parse();
        assertFalse(result.ok());
        assertTrue(result.errors().get(0).message().contains("Invalid integer"));
    }
    
    @Test void invalidBooleanReportsError() {
        ParseResult<ServerConfig> result = new ConfigParser("ssl=maybe").parse();
        assertFalse(result.ok());
        assertTrue(result.errors().get(0).message().contains("Invalid boolean"));
    }
    
    @Test void invalidDurationReportsError() {
        ParseResult<ServerConfig> result = new ConfigParser("timeout=30x").parse();
        assertFalse(result.ok());
        assertTrue(result.errors().get(0).message().contains("Invalid duration"));
    }
    
    @Test void durationSeconds() {
        ParseResult<ServerConfig> result = new ConfigParser("timeout=45s").parse();
        assertTrue(result.ok());
        assertEquals(45_000, result.value().timeout().milliseconds());
    }
    
    @Test void durationMinutes() {
        ParseResult<ServerConfig> result = new ConfigParser("timeout=2m").parse();
        assertTrue(result.ok());
        assertEquals(120_000, result.value().timeout().milliseconds());
    }
    
    @Test void durationHours() {
        ParseResult<ServerConfig> result = new ConfigParser("timeout=1h").parse();
        assertTrue(result.ok());
        assertEquals(3_600_000, result.value().timeout().milliseconds());
    }
}
