import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class ConfigParserTest {
    
    @Test
    void parsesIntValue() {
        Map<String, ConfigKey> schema = Map.of(
            "timeout", new ConfigKey(new IntValue(30), IntValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("timeout = 100");
        
        assertFalse(config.hasErrors());
        assertEquals(100, config.getInt("", "timeout"));
    }
    
    @Test
    void usesDefaultForMissingKey() {
        Map<String, ConfigKey> schema = Map.of(
            "timeout", new ConfigKey(new IntValue(30), IntValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("# comment only");
        
        assertFalse(config.hasErrors());
        assertEquals(30, config.getInt("", "timeout"));
    }
    
    @Test
    void parsesSectionedKeys() {
        Map<String, ConfigKey> schema = Map.of(
            "db.port", new ConfigKey(new IntValue(5432), IntValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("[db]\nport = 3306");
        
        assertFalse(config.hasErrors());
        assertEquals(3306, config.getInt("db", "port"));
    }
    
    @Test
    void parsesBooleanTrue() {
        Map<String, ConfigKey> schema = Map.of(
            "cache.enabled", new ConfigKey(new BoolValue(false), BoolValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("[cache]\nenabled = yes");
        
        assertFalse(config.hasErrors());
        assertTrue(config.getBoolean("cache", "enabled"));
    }
    
    @Test
    void parsesBooleanFalse() {
        Map<String, ConfigKey> schema = Map.of(
            "debug", new ConfigKey(new BoolValue(true), BoolValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("debug = false");
        
        assertFalse(config.hasErrors());
        assertFalse(config.getBoolean("", "debug"));
    }
    
    @Test
    void parsesDurationSeconds() {
        Map<String, ConfigKey> schema = Map.of(
            "timeout", new ConfigKey(new DurationValue(0), DurationValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("timeout = 30s");
        
        assertFalse(config.hasErrors());
        assertEquals(30, config.getDurationSeconds("", "timeout"));
    }
    
    @Test
    void parsesDurationMinutes() {
        Map<String, ConfigKey> schema = Map.of(
            "cache.ttl", new ConfigKey(new DurationValue(0), DurationValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("[cache]\nttl = 5m");
        
        assertFalse(config.hasErrors());
        assertEquals(300, config.getDurationSeconds("cache", "ttl"));
    }
    
    @Test
    void parsesDurationHours() {
        Map<String, ConfigKey> schema = Map.of(
            "retention", new ConfigKey(new DurationValue(0), DurationValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("retention = 2h");
        
        assertFalse(config.hasErrors());
        assertEquals(7200, config.getDurationSeconds("", "retention"));
    }
    
    @Test
    void reportsUnknownKeyWithLineNumber() {
        Map<String, ConfigKey> schema = new HashMap<>();
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("unknown = 42");
        
        assertTrue(config.hasErrors());
        List<ValidationError> errors = config.getErrors();
        assertEquals(1, errors.size());
        assertEquals(1, errors.get(0).lineNumber());
        assertTrue(errors.get(0).message().contains("Unknown key"));
    }
    
    @Test
    void reportsMalformedLineWithLineNumber() {
        Map<String, ConfigKey> schema = new HashMap<>();
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("not a valid line");
        
        assertTrue(config.hasErrors());
        assertEquals(1, config.getErrors().get(0).lineNumber());
        assertTrue(config.getErrors().get(0).message().contains("Malformed"));
    }
    
    @Test
    void reportsTypeErrorWithLineNumber() {
        Map<String, ConfigKey> schema = Map.of(
            "port", new ConfigKey(new IntValue(8080), IntValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("port = not_a_number");
        
        assertTrue(config.hasErrors());
        List<ValidationError> errors = config.getErrors();
        assertEquals(1, errors.size());
        assertEquals(1, errors.get(0).lineNumber());
        assertTrue(errors.get(0).message().contains("Invalid IntValue"));
    }
    
    @Test
    void ignoresCommentsAndBlankLines() {
        Map<String, ConfigKey> schema = Map.of(
            "value", new ConfigKey(new IntValue(0), IntValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("# comment\n\nvalue = 42\n# footer");
        
        assertFalse(config.hasErrors());
        assertEquals(42, config.getInt("", "value"));
    }
    
    @Test
    void multipleErrors() {
        Map<String, ConfigKey> schema = Map.of(
            "known", new ConfigKey(new IntValue(0), IntValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        Configuration config = parser.parse("malformed\nunknown = 1\nknown = bad");
        
        assertTrue(config.hasErrors());
        List<ValidationError> errors = config.getErrors();
        assertEquals(3, errors.size());
        assertEquals(1, errors.get(0).lineNumber());
        assertEquals(2, errors.get(1).lineNumber());
        assertEquals(3, errors.get(2).lineNumber());
    }
    
    @Test
    void multipleSections() {
        Map<String, ConfigKey> schema = Map.of(
            "db.host", new ConfigKey(new IntValue(0), IntValue.class),
            "cache.ttl", new ConfigKey(new DurationValue(0), DurationValue.class)
        );
        ConfigParser parser = new ConfigParser(schema);
        
        String content = "[db]\nhost = 1\n[cache]\nttl = 5m";
        Configuration config = parser.parse(content);
        
        assertFalse(config.hasErrors());
        assertEquals(1, config.getInt("db", "host"));
        assertEquals(300, config.getDurationSeconds("cache", "ttl"));
    }
}
