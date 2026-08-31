import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.time.Duration;

class ConfigParserTest {
    
    @Test
    void parsesSimpleConfig() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("database", "port")
            .addBoolean("database", "enabled");
        
        String input = "[database]\nport = 5432\nenabled = true";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertTrue(config.isValid());
        assertEquals(5432, config.getInt("database", "port", -1));
        assertTrue(config.getBoolean("database", "enabled", false));
    }
    
    @Test
    void handlesBooleanFormats() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addBoolean("test", "v1")
            .addBoolean("test", "v2")
            .addBoolean("test", "v3")
            .addBoolean("test", "v4")
            .addBoolean("test", "v5")
            .addBoolean("test", "v6");
        
        String input = "[test]\nv1 = true\nv2 = yes\nv3 = on\nv4 = false\nv5 = no\nv6 = off";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertTrue(config.isValid());
        assertTrue(config.getBoolean("test", "v1", false));
        assertTrue(config.getBoolean("test", "v2", false));
        assertTrue(config.getBoolean("test", "v3", false));
        assertFalse(config.getBoolean("test", "v4", true));
        assertFalse(config.getBoolean("test", "v5", true));
        assertFalse(config.getBoolean("test", "v6", true));
    }
    
    @Test
    void parsesDurations() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addDuration("timeout", "read")
            .addDuration("timeout", "write")
            .addDuration("timeout", "cache");
        
        String input = "[timeout]\nread = 30s\nwrite = 5m\ncache = 1h";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertTrue(config.isValid());
        assertEquals(Duration.ofSeconds(30), config.getDuration("timeout", "read", null));
        assertEquals(Duration.ofMinutes(5), config.getDuration("timeout", "write", null));
        assertEquals(Duration.ofHours(1), config.getDuration("timeout", "cache", null));
    }
    
    @Test
    void reportsErrorForMalformedLine() {
        ConfigParser.Schema schema = new ConfigParser.Schema();
        
        String input = "[section]\nthis is malformed";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertFalse(config.isValid());
        assertEquals(1, config.getErrors().size());
        assertTrue(config.getErrors().get(0).contains("Line 2"));
    }
    
    @Test
    void reportsErrorForUnknownKey() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("db", "port");
        
        String input = "[db]\nhost = localhost\nport = 5432";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertFalse(config.isValid());
        assertEquals(1, config.getErrors().size());
        assertTrue(config.getErrors().get(0).contains("Line 2"));
        assertTrue(config.getErrors().get(0).contains("unknown key"));
    }
    
    @Test
    void reportsErrorForInvalidType() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("server", "port");
        
        String input = "[server]\nport = not_a_number";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertFalse(config.isValid());
        assertTrue(config.getErrors().get(0).contains("Line 2"));
        assertTrue(config.getErrors().get(0).contains("invalid"));
    }
    
    @Test
    void ignoresCommentsAndBlankLines() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("server", "port");
        
        String input = "# This is a comment\n\n[server]\n# another\nport = 8080\n\n# end";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertTrue(config.isValid());
        assertEquals(8080, config.getInt("server", "port", -1));
    }
    
    @Test
    void usesDefaultsForMissingKeys() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("db", "port");
        
        String input = "[other]";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertTrue(config.isValid());
        assertEquals(5432, config.getInt("db", "port", 5432));
    }
    
    @Test
    void reportsErrorForKeyOutsideSection() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("db", "port");
        
        String input = "port = 8080\n[db]";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertFalse(config.isValid());
        assertTrue(config.getErrors().get(0).contains("Line 1"));
        assertTrue(config.getErrors().get(0).contains("outside section"));
    }
    
    @Test
    void parsesNegativeIntegers() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("calc", "offset");
        
        String input = "[calc]\noffset = -42";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertTrue(config.isValid());
        assertEquals(-42, config.getInt("calc", "offset", 0));
    }
    
    @Test
    void trimsWhitespace() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("server", "port");
        
        String input = "[server]  \n  port  =  8080  ";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertTrue(config.isValid());
        assertEquals(8080, config.getInt("server", "port", -1));
    }
    
    @Test
    void collectsMultipleErrors() {
        ConfigParser.Schema schema = new ConfigParser.Schema()
            .addInt("db", "port");
        
        String input = "bad line 1\n[db]\nbad line 2\nport = abc\nbad line 3";
        ConfigParser.Config config = ConfigParser.parse(input, schema);
        
        assertFalse(config.isValid());
        assertEquals(3, config.getErrors().size());
    }
}
