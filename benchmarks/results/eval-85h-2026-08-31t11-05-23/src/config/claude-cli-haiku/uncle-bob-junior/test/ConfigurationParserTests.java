import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigurationParserTests {
    private final ConfigurationParser parser = new ConfigurationParser();
    
    @Test
    void parseValidServerConfig() {
        String input = "[server]\nport=9000\n";
        ParseResult<Configuration> result = parser.parse(input);
        assertTrue(result.isSuccess());
        assertEquals(9000, result.getValue().getServer().getPort());
    }
    
    @Test
    void parseValidDatabaseConfig() {
        String input = "[database]\nmax_connections=50\n";
        ParseResult<Configuration> result = parser.parse(input);
        assertTrue(result.isSuccess());
        assertEquals(50, result.getValue().getDatabase().getMaxConnections());
    }
    
    @Test
    void parseDurationInSeconds() {
        String input = "[server]\ntimeout=45s\n";
        ParseResult<Configuration> result = parser.parse(input);
        assertTrue(result.isSuccess());
        assertEquals(45, result.getValue().getServer().getTimeout().toSeconds());
    }
    
    @Test
    void parseDurationInMinutes() {
        String input = "[database]\nconnection_timeout=2m\n";
        ParseResult<Configuration> result = parser.parse(input);
        assertTrue(result.isSuccess());
        assertEquals(120, result.getValue().getDatabase().getConnectionTimeout().toSeconds());
    }
    
    @Test
    void parseBoolean() {
        String input = "[server]\ndebug=true\n";
        ParseResult<Configuration> result = parser.parse(input);
        assertTrue(result.isSuccess());
        assertTrue(result.getValue().getServer().isDebug());
    }
    
    @Test
    void ignoreCommentsAndBlankLines() {
        String input = "# Comment\n\n[server]\n# Another comment\nport=8080\n\n";
        ParseResult<Configuration> result = parser.parse(input);
        assertTrue(result.isSuccess());
        assertEquals(8080, result.getValue().getServer().getPort());
    }
    
    @Test
    void useDefaultsForMissingKeys() {
        String input = "[server]\nport=8080\n";
        ParseResult<Configuration> result = parser.parse(input);
        assertTrue(result.isSuccess());
        assertEquals(30, result.getValue().getServer().getTimeout().toSeconds());
        assertFalse(result.getValue().getServer().isDebug());
    }
    
    @Test
    void malformedLineProducesError() {
        String input = "[server]\ninvalid line without equals\n";
        ParseResult<Configuration> result = parser.parse(input);
        assertFalse(result.isSuccess());
        assertEquals(1, result.getErrors().size());
        assertEquals(2, result.getErrors().get(0).getLineNumber());
    }
    
    @Test
    void unknownKeyProducesError() {
        String input = "[server]\nunknown_key=value\n";
        ParseResult<Configuration> result = parser.parse(input);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().get(0).getMessage().contains("Unknown key"));
    }
    
    @Test
    void invalidIntegerProducesError() {
        String input = "[server]\nport=not_a_number\n";
        ParseResult<Configuration> result = parser.parse(input);
        assertFalse(result.isSuccess());
        assertEquals(2, result.getErrors().get(0).getLineNumber());
    }
    
    @Test
    void keyValueOutsideSectionProducesError() {
        String input = "key=value\n[server]\n";
        ParseResult<Configuration> result = parser.parse(input);
        assertFalse(result.isSuccess());
        assertEquals(1, result.getErrors().get(0).getLineNumber());
        assertTrue(result.getErrors().get(0).getMessage().contains("outside section"));
    }
    
    @Test
    void invalidDurationProducesError() {
        String input = "[server]\ntimeout=30x\n";
        ParseResult<Configuration> result = parser.parse(input);
        assertFalse(result.isSuccess());
    }
    
    @Test
    void multipleErrorsReported() {
        String input = "[server]\nport=abc\nmalformed\nunknown=x\n";
        ParseResult<Configuration> result = parser.parse(input);
        assertFalse(result.isSuccess());
        assertEquals(3, result.getErrors().size());
    }
    
    @Test
    void complexValidConfig() {
        String input = "[server]\nport=9000\ntimeout=60s\ndebug=false\n\n[database]\nmax_connections=100\nconnection_timeout=5m\n";
        ParseResult<Configuration> result = parser.parse(input);
        
        assertTrue(result.isSuccess());
        ServerConfig server = result.getValue().getServer();
        DatabaseConfig database = result.getValue().getDatabase();
        
        assertEquals(9000, server.getPort());
        assertEquals(60, server.getTimeout().toSeconds());
        assertFalse(server.isDebug());
        assertEquals(100, database.getMaxConnections());
        assertEquals(300, database.getConnectionTimeout().toSeconds());
    }
}
