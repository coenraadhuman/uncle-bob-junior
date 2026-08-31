public class Schema {
    private final Map<String, SectionSchema> sections;
    
    public Schema(Map<String, SectionSchema> sections) {
        this.sections = Map.copyOf(sections);
    }
    
    public SectionSchema getSection(String name) { return sections.get(name); }
    public Set<String> sectionNames() { return sections.keySet(); }
}
