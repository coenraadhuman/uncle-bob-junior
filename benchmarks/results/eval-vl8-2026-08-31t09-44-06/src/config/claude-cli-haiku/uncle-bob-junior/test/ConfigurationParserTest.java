import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationParserTest {
    
    @Test
    public void parsesValidConfiguration() {
        String config = "[server]\nport = 9000\nthreads = 20";
        
        ConfigSchema schema = new ConfigSchema();
        schema.addInt("server.port", 8080);
        schema.addInt("server.threads", 10);
        
        ConfigurationParser parser = new ConfigurationParser(config, schema);
        ParseResult result = parser.parse();
        
        assertTrue(result.isSuccess());
        assertEquals(9000, result.configuration().getInt("server.port"));
        assertEquals(20, result.configuration().getInt("server.threads"));
    }
    
    @Test
    public void handlesCommentsAndBlankLines() {
        String config = "# Configuration file\n[server]\n\n# Port setting\nport = 3000\n# End";
        
        ConfigSchema schema = new ConfigSchema();
        schema.addInt("server.port", 8080);
        
        ConfigurationParser parser = new ConfigurationParser(config, schema);
        ParseResult result = parser.parse();
        
        assertTrue(result.isSuccess());
        assertEquals(3000, result.configuration().getInt("server.port"));
    }
    
    @Test
    public void parsesBooleanValues() {
        String config = "[app]\ndebug = true\nenabled = false";
        
        ConfigSchema schema = new ConfigSchema();
        schema.addBoolean("app.debug", false);
        schema.addBoolean("app.enabled", true);
        
        ConfigurationParser parser = new ConfigurationParser(config, schema);
        ParseResult result = parser.parse();
        
        assertTrue(result.isSuccess());
        assertTrue(result.configuration().getBoolean("app.debug"));
        assertFalse(result.configuration().getBoolean("app.enabled"));
    }
    
    @Test
    public void parsesDurationValues() {
        String config = "[timeouts]\nshort = 30s\nlong = 5m";
        
        ConfigSchema schema = new ConfigSchema();
        schema.addDuration("timeouts.short", Duration.ofSeconds(60));
        schema.addDuration("timeouts.long", Duration.ofMinutes(1));
        
        ConfigurationParser parser = new ConfigurationParser(config, schema);
        ParseResult result = parser.parse();
        
        assertTrue(result.isSuccess());
        assertEquals(Duration.ofSeconds(30), result.configuration().getDuration("timeouts.short"));
        assertEquals(Duration.ofMinutes(5), result.configuration().getDuration("timeouts.long"));
    }
    
    @Test
    public void usesDefaultsForMissingKeys() {
        String config = "[server]\nport = 9000";
        
        ConfigSchema schema = new ConfigSchema();
        schema.addInt("server.port", 8080);
        schema.addInt("server.threads", 10);
        
        ConfigurationParser parser = new ConfigurationParser(config, schema);
        ParseResult result = parser.parse();
        
        assertTrue(result.isSuccess());
        assertEquals(9000, result.configuration().getInt("server.port"));
        assertEquals(10, result.configuration().getInt("server.threads"));
    }
    
    @Test
    public void reportsMalformedLines() {
        String config = "[server]\nmalformed line";
        
        ConfigSchema schema = new ConfigSchema();
        ConfigurationParser parser = new ConfigurationParser(config, schema);
        ParseResult result = parser.parse();
        
        assertFalse(result.isSuccess());
        assertTrue(result.errors().get(0).contains("Malformed line 2"));
    }
    
    @Test
    public void reportsUnknownKeys() {
        String config = "[server]\nunknown = 123";
        
        ConfigSchema schema = new ConfigSchema();
        ConfigurationParser parser = new ConfigurationParser(config, schema);
        ParseResult result = parser.parse();
        
        assertFalse(result.isSuccess());
        assertTrue(result.errors().get(0).contains("Unknown key at line 1"));
        assertTrue(result.errors().get(0).contains("server.unknown"));
    }
    
    @Test
    public void reportsInvalidIntegerValue() {
        String config = "[server]\nport = not_a_number";
        
        ConfigSchema schema = new ConfigSchema();
        schema.addInt("server.port", 8080);
        
        ConfigurationParser parser = new ConfigurationParser(config, schema);
        ParseResult result = parser.parse();
        
        assertFalse(result.isSuccess());
        assertTrue(result.errors().get(0).contains("Invalid value at line 1"));
        assertTrue(result.errors().get(0).contains("server.port"));
    }
    
    @Test
    public void reportsInvalidDurationFormat() {
        String config = "[app]\nwait = 30x";
        
        ConfigSchema schema = new ConfigSchema();
        schema.addDuration("app.wait", Duration.ofSeconds(60));
        
        ConfigurationParser parser = new ConfigurationParser(config, schema);
        ParseResult result = parser.parse();
        
        assertFalse(result.isSuccess());
        assertTrue(result.errors().get(0).contains("Invalid value at line 1"));
    }
    
    @Test
    public void collectsMultipleErrors() {
        String config = "[app]\nmalformed1\nmalformed2\nunknown = x";
        
        ConfigSchema schema = new ConfigSchema();
        ConfigurationParser parser = new ConfigurationParser(config, schema);
        ParseResult result = parser.parse();
        
        assertFalse(result.isSuccess());
        assertEquals(3, result.errors().size());
    }
    
    @Test
    public void reportsErrorLineNumbers() {
        String config = "[server]\nport = 9000\n# Comment\nunknown = 5";
        
        ConfigSchema schema = new ConfigSchema();
        schema.addInt("server.port", 8080);
        
        ConfigurationParser parser = new ConfigurationParser(config, schema);
        ParseResult result = parser.parse();
        
        assertFalse(result.isSuccess());
        assertTrue(result.errors().get(0).contains("line 4"));
    }
}
