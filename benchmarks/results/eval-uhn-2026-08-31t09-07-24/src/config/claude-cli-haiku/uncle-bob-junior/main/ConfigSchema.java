import java.util.*;

class ConfigSchema {
    private final Map<String, SectionSpec> sections = new LinkedHashMap<>();
    
    void addSection(SectionSpec spec) {
        sections.put(spec.name(), spec);
    }
    
    SectionSpec section(String name) { return sections.get(name); }
    boolean hasSection(String name) { return sections.containsKey(name); }
    Collection<SectionSpec> sections() { return sections.values(); }
}
