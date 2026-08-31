import java.util.*;

class ConfigurationParser {
  private final ConfigSchema schema;
  
  ConfigurationParser(ConfigSchema schema) {
    this.schema = schema;
  }
  
  Configuration parse(String input) throws ParsingException {
    List<String> lines = input.split("\n", -1);
    List<ValidationError> errors = new ArrayList<>();
    Map<String, Map<String, ConfigValue>> sections = new HashMap<>();
    String currentSection = null;
    
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i).trim();
      int lineNumber = i + 1;
      
      if (isBlank(line) || isComment(line)) continue;
      
      if (isSection(line)) {
        currentSection = extractSection(line);
        if (!schema.isKnownSection(currentSection)) {
          errors.add(new ValidationError(lineNumber, "Unknown section: " + currentSection));
        }
        continue;
      }
      
      String[] pair = parseKeyValue(line);
      if (pair == null) {
        errors.add(new ValidationError(lineNumber, "Malformed line: expected 'key=value'"));
        continue;
      }
      
      String key = pair[0];
      if (!schema.isKnownKey(key)) {
        errors.add(new ValidationError(lineNumber, "Unknown key: " + key));
        continue;
      }
      
      ConfigValue value = parseTypedValue(key, pair[1], lineNumber, errors);
      if (value != null && currentSection != null) {
        sections.computeIfAbsent(currentSection, k -> new HashMap<>()).put(key, value);
      }
    }
    
    if (!errors.isEmpty()) {
      throw new ParsingException(errors);
    }
    
    return new Configuration(sections, schema.defaults());
  }
  
  private boolean isBlank(String line) {
    return line.isEmpty();
  }
  
  private boolean isComment(String line) {
    return line.startsWith("#");
  }
  
  private boolean isSection(String line) {
    return line.startsWith("[") && line.endsWith("]");
  }
  
  private String extractSection(String line) {
    return line.substring(1, line.length() - 1).trim();
  }
  
  private String[] parseKeyValue(String line) {
    int eqIdx = line.indexOf('=');
    if (eqIdx <= 0) return null;
    
    String key = line.substring(0, eqIdx).trim();
    String value = line.substring(eqIdx + 1).trim();
    
    if (key.isEmpty()) return null;
    return new String[]{key, value};
  }
  
  private ConfigValue parseTypedValue(String key, String valueStr, int lineNumber, List<ValidationError> errors) {
    ConfigValue defaultValue = schema.defaults().get(key);
    
    if (defaultValue instanceof ConfigValue.IntValue) {
      return parseIntValue(key, valueStr, lineNumber, errors);
    }
    if (defaultValue instanceof ConfigValue.BoolValue) {
      return parseBoolValue(key, valueStr, lineNumber, errors);
    }
    if (defaultValue instanceof ConfigValue.DurationValue) {
      return parseDurationValue(key, valueStr, lineNumber, errors);
    }
    
    return null;
  }
  
  private ConfigValue.IntValue parseIntValue(String key, String valueStr, int lineNumber, List<ValidationError> errors) {
    try {
      return new ConfigValue.IntValue(Integer.parseInt(valueStr));
    } catch (NumberFormatException e) {
      errors.add(new ValidationError(lineNumber, "Invalid integer for " + key + ": " + valueStr));
      return null;
    }
  }
  
  private ConfigValue.BoolValue parseBoolValue(String key, String valueStr, int lineNumber, List<ValidationError> errors) {
    String lower = valueStr.toLowerCase();
    if ("true".equals(lower) || "yes".equals(lower)) {
      return new ConfigValue.BoolValue(true);
    }
    if ("false".equals(lower) || "no".equals(lower)) {
      return new ConfigValue.BoolValue(false);
    }
    errors.add(new ValidationError(lineNumber, "Invalid boolean for " + key + ": " + valueStr));
    return null;
  }
  
  private ConfigValue.DurationValue parseDurationValue(String key, String valueStr, int lineNumber, List<ValidationError> errors) {
    long ms = parseDurationToMs(valueStr);
    if (ms < 0) {
      errors.add(new ValidationError(lineNumber, "Invalid duration for " + key + ": " + valueStr));
      return null;
    }
    return new ConfigValue.DurationValue(ms);
  }
  
  private long parseDurationToMs(String input) {
    input = input.trim();
    if (input.isEmpty()) return -1;
    
    for (int i = input.length() - 1; i >= 0; i--) {
      if (!Character.isDigit(input.charAt(i))) {
        String numStr = input.substring(0, i).trim();
        String unit = input.substring(i).toLowerCase().trim();
        
        try {
          long value = Long.parseLong(numStr);
          return switch (unit) {
            case "ms" -> value;
            case "s" -> value * 1_000;
            case "m" -> value * 60_000;
            case "h" -> value * 3_600_000;
            default -> -1;
          };
        } catch (NumberFormatException e) {
          return -1;
        }
      }
    }
    
    return -1;
  }
}
