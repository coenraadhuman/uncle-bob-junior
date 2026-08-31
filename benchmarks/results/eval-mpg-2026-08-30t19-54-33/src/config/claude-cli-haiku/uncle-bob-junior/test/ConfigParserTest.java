import java.util.*;

class ConfigParserTest {
  void testParseValidIntegers() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("timeout=30\nmax_retries=5");
    assert result.isValid() : "Should parse valid integers";
    assert result.config().getInt("timeout") == 30;
    assert result.config().getInt("max_retries") == 5;
  }

  void testParseValidBooleans() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("enabled=true\ndebug=false");
    assert result.isValid();
    assert result.config().getBoolean("enabled");
    assert !result.config().getBoolean("debug");
  }

  void testParseValidDurations() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("timeout=30s\nmax_wait=5m\nheartbeat=500ms");
    assert result.isValid();
    assert result.config().getDuration("timeout").milliseconds() == 30_000;
    assert result.config().getDuration("max_wait").milliseconds() == 300_000;
    assert result.config().getDuration("heartbeat").milliseconds() == 500;
  }

  void testApplyDefaultsForMissingKeys() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("timeout=30");
    assert result.isValid();
    assert result.config().getInt("max_retries") == 10 : "Should use default";
  }

  void testIgnoreCommentsAndBlankLines() {
    ConfigParser parser = createParser();
    String config = "# This is a comment\ntimeout=30\n\n# Another\nmax_retries=5";
    ConfigParseResult result = parser.parse(config);
    assert result.isValid();
    assert result.config().getInt("timeout") == 30;
  }

  void testIgnoreSections() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("[database]\ntimeout=30\n[server]\nmax_retries=5");
    assert result.isValid();
  }

  void testErrorOnMalformedLine() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("timeout=30\nmissing_equals\nmax_retries=5");
    assert !result.isValid() : "Should detect malformed line";
    assert result.errors().size() == 1;
    assert result.errors().get(0).line() == 2 : "Error on line 2";
    assert result.errors().get(0).key().equals("syntax");
  }

  void testErrorOnUnknownKey() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("unknown_setting=123");
    assert !result.isValid();
    assert result.errors().get(0).key().equals("unknown_setting");
    assert result.errors().get(0).message().contains("Unknown");
  }

  void testErrorOnInvalidIntegerValue() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("timeout=not_a_number");
    assert !result.isValid();
    assert result.errors().get(0).key().equals("timeout");
    assert result.errors().get(0).message().contains("integer");
  }

  void testErrorOnInvalidBooleanValue() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("enabled=yes");
    assert !result.isValid();
    assert result.errors().get(0).message().contains("boolean");
  }

  void testErrorOnInvalidDurationValue() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("timeout=30");
    assert !result.isValid();
    assert result.errors().get(0).message().contains("duration");
  }

  void testCaseInsensitiveBooleans() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("enabled=TRUE\ndebug=FALSE");
    assert result.isValid();
    assert result.config().getBoolean("enabled");
    assert !result.config().getBoolean("debug");
  }

  void testMultipleErrors() {
    ConfigParser parser = createParser();
    String config = "invalid line\ntimeout=abc\nunknown=99";
    ConfigParseResult result = parser.parse(config);
    assert !result.isValid();
    assert result.errors().size() == 3 : "Should collect all errors";
    assert result.errors().get(0).line() == 1;
    assert result.errors().get(1).line() == 2;
    assert result.errors().get(2).line() == 3;
  }

  void testWhitespaceHandling() {
    ConfigParser parser = createParser();
    ConfigParseResult result = parser.parse("timeout = 30 \n max_retries = 5");
    assert result.isValid() : "Should handle whitespace around =";
    assert result.config().getInt("timeout") == 30;
  }

  private ConfigParser createParser() {
    return new ConfigParser(List.of(
      new ConfigKey("timeout", Duration.class, new Duration(5000)),
      new ConfigKey("max_retries", Integer.class, 10),
      new ConfigKey("enabled", Boolean.class, true),
      new ConfigKey("debug", Boolean.class, false)
    ));
  }
}
