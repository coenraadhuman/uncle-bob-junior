public class Config {
    private final Map<String, Section> sections;
    
    Config(Map<String, Section> sections) {
        this.sections = Map.copyOf(sections);
    }
    
    public Section getSection(String name) {
        var section = sections.get(name);
        if (section == null) throw new IllegalArgumentException("Unknown section: " + name);
        return section;
    }
}
