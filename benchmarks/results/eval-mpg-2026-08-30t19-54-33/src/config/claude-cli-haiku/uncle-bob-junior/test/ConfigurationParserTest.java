import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class ConfigurationParserTest {
  
  @Test
  void parsesSimpleConfig() throws ParsingException {
    ConfigSchema schema = new ConfigSchema();
    schema.addInt("timeout", 30);
    schema.addBoolean("debug", false);
    schema.addDuration("wait", 5000);
    schema.addSection("server");
    
    String input = """
      [server]
      timeout=60
      debug=true
      wait=2s
      """;
    
    ConfigurationParser parser = new ConfigurationParser(schema);
    Configuration config = parser.parse(input);
    
    assertEquals(60, config.getInt("server", "timeout"));
    assertTrue(config.getBoolean("server", "debug"));
    assertEquals(2_000, config.getDurationMs("server", "wait"));
  }
  
  @Test
  void appliesDefaultsForMissingKeys() throws ParsingException {
    ConfigSchema schema = new ConfigSchema();
    schema.addInt("timeout", 30);
    schema.addBoolean("debug", false);
    schema.addSection("server");
    
    String input = "[server]\n";
    
    ConfigurationParser parser = new ConfigurationParser(schema);
    Configuration config = parser.parse(input);
    
    assertEquals(30, config.getInt("server", "timeout"));
    assertFalse(config.getBoolean("server", "debug"));
  }
  
  @Test
  void reportsMalformedLines() {
    ConfigSchema schema = new ConfigSchema();
    schema.addInt("timeout", 30);
    schema.addSection("server");
    
    String input = """
      [server]
      timeout 60
      invalid line
      timeout=45
      """;
    
    ConfigurationParser parser = new ConfigurationParser(schema);
    ParsingException ex = assertThrows(ParsingException.class, () -> parser.parse(input));
    
    List<ValidationError> errors = ex.errors();
    assertEquals(2, errors.size());
    assertTrue(errors.stream().anyMatch(e -> e.lineNumber() == 2 && e.message().contains("Malformed")));
    assertTrue(errors.stream().anyMatch(e -> e.lineNumber() == 3 && e.message().contains("Malformed")));
  }
  
  @Test
  void reportsUnknownKeys() {
    ConfigSchema schema = new ConfigSchema();
    schema.addInt("timeout", 30);
    schema.addSection("server");
    
    String input = """
      [server]
      timeout=60
      unknown=value
      """;
    
    ConfigurationParser parser = new ConfigurationParser(schema);
    ParsingException ex = assertThrows(ParsingException.class, () -> parser.parse(input));
    
    List<ValidationError> errors = ex.errors();
    assertTrue(errors.stream().anyMatch(e -> e.lineNumber() == 3 && e.message().contains("Unknown key")));
  }
  
  @Test
  void reportsUnknownSections() {
    ConfigSchema schema = new ConfigSchema();
    schema.addInt("timeout", 30);
    schema.addSection("server");
    
    String input = """
      [database]
      timeout=60
      """;
    
    ConfigurationParser parser = new ConfigurationParser(schema);
    ParsingException ex = assertThrows(ParsingException.class, () -> parser.parse(input));
    
    List<ValidationError> errors = ex.errors();
    assertTrue(errors.stream().anyMatch(e -> e.lineNumber() == 1 && e.message().contains("Unknown section")));
  }
  
  @Test
  void ignoresCommentsAndBlankLines() throws ParsingException {
    ConfigSchema schema = new ConfigSchema();
    schema.addInt("timeout", 30);
    schema.addSection("server");
    
    String input = """
      # This is a comment
      [server]
      
      # Another comment
      timeout=60
      """;
    
    ConfigurationParser parser = new ConfigurationParser(schema);
    Configuration config = parser.parse(input);
    
    assertEquals(60, config.getInt("server", "timeout"));
  }
  
  @Test
  void parsesDurations() throws ParsingException {
    ConfigSchema schema = new ConfigSchema();
    schema.addDuration("d1", 1000);
    schema.addDuration("d2", 1000);
    schema.addDuration("d3", 1000);
    schema.addDuration("d4", 1000);
    schema.addSection("timing");
    
    String input = """
      [timing]
      d1=500ms
      d2=30s
      d3=5m
      d4=1h
      """;
    
    ConfigurationParser parser = new ConfigurationParser(schema);
    Configuration config = parser.parse(input);
    
    assertEquals(500, config.getDurationMs("timing", "d1"));
    assertEquals(30_000, config.getDurationMs("timing", "d2"));
    assertEquals(300_000, config.getDurationMs("timing", "d3"));
    assertEquals(3_600_000, config.getDurationMs("timing", "d4"));
  }
  
  @Test
  void reportsInvalidTypes() {
    ConfigSchema schema = new ConfigSchema();
    schema.addInt("count", 0);
    schema.addBoolean("enabled", false);
    schema.addDuration("timeout", 1000);
    schema.addSection("config");
    
    String input = """
      [config]
      count=abc
      enabled=maybe
      timeout=infinite
      """;
    
    ConfigurationParser parser = new ConfigurationParser(schema);
    ParsingException ex = assertThrows(ParsingException.class, () -> parser.parse(input));
    
    List<ValidationError> errors = ex.errors();
    assertEquals(3, errors.size());
    assertTrue(errors.stream().anyMatch(e -> e.lineNumber() == 2 && e.message().contains("Invalid integer")));
    assertTrue(errors.stream().anyMatch(e -> e.lineNumber() == 3 && e.message().contains("Invalid boolean")));
    assertTrue(errors.stream().anyMatch(e -> e.lineNumber() == 4 && e.message().contains("Invalid duration")));
  }
  
  @Test
  void handlesMultipleSections() throws ParsingException {
    ConfigSchema schema = new ConfigSchema();
    schema.addInt("timeout", 30);
    schema.addSection("server");
    schema.addSection("client");
    
    String input = """
      [server]
      timeout=60
      [client]
      timeout=10
      """;
    
    ConfigurationParser parser = new ConfigurationParser(schema);
    Configuration config = parser.parse(input);
    
    assertEquals(60, config.getInt("server", "timeout"));
    assertEquals(10, config.getInt("client", "timeout"));
  }
  
  @Test
  void reportsMultipleErrorsTogether() {
    ConfigSchema schema = new ConfigSchema();
    schema.addInt("a", 0);
    schema.addInt("b", 0);
    schema.addInt("c", 0);
    schema.addSection("test");
    
    String input = """
      [test]
      a=invalid
      b=also bad
      c=still broken
      """;
    
    ConfigurationParser parser = new ConfigurationParser(schema);
    ParsingException ex = assertThrows(ParsingException.class, () -> parser.parse(input));
    
    List<ValidationError> errors = ex.errors();
    assertEquals(3, errors.size());
    assertTrue(errors.stream().allMatch(e -> e.lineNumber() >= 2 && e.lineNumber() <= 4));
  }
}
