public class ConfigParser {
    private static final Set<String> KNOWN_KEYS = Set.of("port", "ssl", "timeout");
    private final List<String> lines;
    
    public ConfigParser(String text) {
        this.lines = text.lines().toList();
    }
    
    public ParseResult<ServerConfig> parse() {
        Map<String, String> values = new HashMap<>();
        List<ParseError> errors = new ArrayList<>();
        
        for (int i = 0; i < lines.size(); i++) {
            parseLine(lines.get(i), i + 1, values, errors);
        }
        
        if (!errors.isEmpty()) {
            return ParseResult.error(errors);
        }
        
        return buildConfig(values);
    }
    
    private void parseLine(String raw, int lineNum, Map<String, String> values, List<ParseError> errors) {
        String line = stripComment(raw).strip();
        
        if (line.isEmpty() || isSection(line)) {
            return;
        }
        
        int eqIdx = line.indexOf('=');
        if (eqIdx < 0) {
            errors.add(new ParseError(lineNum, "Malformed line: missing '='"));
            return;
        }
        
        String key = line.substring(0, eqIdx).strip();
        String value = line.substring(eqIdx + 1).strip();
        
        if (key.isEmpty()) {
            errors.add(new ParseError(lineNum, "Empty key"));
            return;
        }
        
        if (!KNOWN_KEYS.contains(key)) {
            errors.add(new ParseError(lineNum, "Unknown key: " + key));
            return;
        }
        
        values.put(key, value);
    }
    
    private ParseResult<ServerConfig> buildConfig(Map<String, String> values) {
        try {
            int port = parseInt(values.get("port"), 8080);
            boolean ssl = parseBoolean(values.get("ssl"), false);
            Duration timeout = parseDuration(values.get("timeout"), "30s");
            
            return ParseResult.ok(new ServerConfig(port, ssl, timeout));
        } catch (Exception e) {
            return ParseResult.error(List.of(new ParseError(0, e.getMessage())));
        }
    }
    
    private int parseInt(String raw, int defaultVal) {
        if (raw == null) return defaultVal;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer: " + raw);
        }
    }
    
    private boolean parseBoolean(String raw, boolean defaultVal) {
        if (raw == null) return defaultVal;
        if ("true".equalsIgnoreCase(raw)) return true;
        if ("false".equalsIgnoreCase(raw)) return false;
        throw new IllegalArgumentException("Invalid boolean: " + raw);
    }
    
    private Duration parseDuration(String raw, String defaultRaw) {
        if (raw == null) raw = defaultRaw;
        try {
            return Duration.parse(raw);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid duration: " + raw);
        }
    }
    
    private String stripComment(String line) {
        int idx = line.indexOf('#');
        return idx < 0 ? line : line.substring(0, idx);
    }
    
    private boolean isSection(String line) {
        return line.startsWith("[") && line.endsWith("]");
    }
}
