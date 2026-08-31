import java.util.*;

public record ConfigSchema(Map<String, Map<String, ConfigField>> sections) {
  public ConfigField field(String section, String key) throws ParseException {
    Map<String, ConfigField> sectionFields = sections.get(section);
    if (sectionFields == null) {
      throw new ParseException("Unknown section: " + section);
    }
    ConfigField field = sectionFields.get(key);
    if (field == null) {
      throw new ParseException("Unknown key: " + key + " in section: " + section);
    }
    return field;
  }
}
