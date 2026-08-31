import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigParserTest {
    
    private ConfigSchema buildSchema() {
        ConfigSchema schema = new ConfigSchema();
        schema.addSection("server");
        schema.addKey("server", "port", Integer.class, null);
        schema.addKey("server", "enabled", Boolean.class, true);
        schema.addKey("server", "timeout", ConfigDuration.class, null);
        return schema;
    }
    
    @Test
    void parsesInteger() throws ConfigurationException {
        ConfigParser parser = new ConfigParser(buildSchema());
        Configuration config = parser.parse("[server]\nport=8080\n");
        assertEquals(8080, config.getInt("server", "port"));
    }
    
    @Test
    void parsesBoolean() throws ConfigurationException {
        ConfigParser parser = new ConfigParser(buildSchema());
        Configuration config = parser.parse("[server]\nenabled=false\n");
        assertFalse(config.getBoolean("server", "enabled"));
    }
    
    @Test
    void parsesDuration() throws ConfigurationException {
        ConfigParser parser = new ConfigParser(buildSchema());
        Configuration config = parser.parse("[server]\ntimeout=30s\n");
        assertEquals(30000, config.getDuration("server", "timeout").getMillis());
    }
    
    @Test
    void appliesDefaults() throws ConfigurationException {
        ConfigParser parser = new ConfigParser(buildSchema());
        Configuration config = parser.parse("[server]\nport=9000\n");
        assertTrue(config.getBoolean("server", "enabled"));
    }
    
    @Test
    void ignoresCommentsAndBlankLines() throws ConfigurationException {
        ConfigParser parser = new ConfigParser(buildSchema());
        String input = "# comment\n[server]\n\nport=8080 # port value\nenabled=no\n";
        Configuration config = parser.parse(input);
        assertEquals(8080, config.getInt("server", "port"));
        assertFalse(config.getBoolean("server", "enabled"));
    }
    
    @Test
    void reportUnknownSection() {
        ConfigParser parser = new ConfigParser(buildSchema());
        ConfigurationException ex = assertThrows(ConfigurationException.class, () -> parser.parse("[unknown]\nport=8080\n"));
        assertEquals(1, ex.getLineNumber());
        assertTrue(ex.getMessage().contains("Unknown section"));
    }
    
    @Test
    void reportUnknownKey() {
        ConfigParser parser = new ConfigParser(buildSchema());
        ConfigurationException ex = assertThrows(ConfigurationException.class, () -> parser.parse("[server]\nunknown=value\n"));
        assertEquals(2, ex.getLineNumber());
        assertTrue(ex.getMessage().contains("Unknown key"));
    }
    
    @Test
    void reportMalformedLine() {
        ConfigParser parser = new ConfigParser(buildSchema());
        ConfigurationException ex = assertThrows(ConfigurationException.class, () -> parser.parse("[server]\ninvalid line here\n"));
        assertEquals(2, ex.getLineNumber());
        assertTrue(ex.getMessage().contains("Malformed"));
    }
    
    @Test
    void reportInvalidInteger() {
        ConfigParser parser = new ConfigParser(buildSchema());
        assertThrows(ConfigurationException.class, () -> parser.parse("[server]\nport=not_a_number\n"));
    }
    
    @Test
    void reportInvalidDuration() {
        ConfigParser parser = new ConfigParser(buildSchema());
        assertThrows(ConfigurationException.class, () -> parser.parse("[server]\ntimeout=xyz\n"));
    }
    
    @Test
    void reportKeyValueOutsideSection() {
        ConfigParser parser = new ConfigParser(buildSchema());
        ConfigurationException ex = assertThrows(ConfigurationException.class, () -> parser.parse("port=8080\n"));
        assertEquals(1, ex.getLineNumber());
        assertTrue(ex.getMessage().contains("outside section"));
    }
    
    @Test
    void parsesAllDurationUnits() throws ConfigurationException {
        ConfigParser parser = new ConfigParser(buildSchema());
        assertEquals(30000, parser.parse("[server]\ntimeout=30s\n").getDuration("server", "timeout").getMillis());
        assertEquals(300000, parser.parse("[server]\ntimeout=5m\n").getDuration("server", "timeout").getMillis());
        assertEquals(7200000, parser.parse("[server]\ntimeout=2h\n").getDuration("server", "timeout").getMillis());
        assertEquals(86400000, parser.parse("[server]\ntimeout=1d\n").getDuration("server", "timeout").getMillis());
    }
    
    @Test
    void parsesBooleanVariants() throws ConfigurationException {
        ConfigParser parser = new ConfigParser(buildSchema());
        String[] trueValues = {"true", "yes", "on", "1"};
        for (String val : trueValues) {
            assertTrue(parser.parse("[server]\nenabled=" + val + "\n").getBoolean("server", "enabled"));
        }
        String[] falseValues = {"false", "no", "off", "0"};
        for (String val : falseValues) {
            assertFalse(parser.parse("[server]\nenabled=" + val + "\n").getBoolean("server", "enabled"));
        }
    }
}
