import java.util.*;

public class Config {
    private final Map<String, Map<String, Object>> sections;
    private final List<String> errors;

    public Config() {
        this.sections = new HashMap<>();
        this.errors = new ArrayList<>();
    }

    void addSection(String name) {
        sections.putIfAbsent(name, new HashMap<>());
    }

    void setValue(String section, String key, Object value) {
        sections.computeIfAbsent(section, k -> new HashMap<>()).put(key, value);
    }

    public Object getValue(String section, String key) {
        return sections.getOrDefault(section, Collections.emptyMap()).get(key);
    }

    public Integer getInt(String section, String key) {
        Object val = getValue(section, key);
        return val instanceof Integer ? (Integer) val : null;
    }

    public Boolean getBoolean(String section, String key) {
        Object val = getValue(section, key);
        return val instanceof Boolean ? (Boolean) val : null;
    }

    void addError(String error) {
        errors.add(error);
    }

    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    @Override
    public String toString() {
        return "Config{sections=" + sections + ", errors=" + errors + '}';
    }
}
