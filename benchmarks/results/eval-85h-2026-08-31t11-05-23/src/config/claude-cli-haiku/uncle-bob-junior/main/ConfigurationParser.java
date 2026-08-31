public class ConfigurationParser {
    private static final String SECTION_REGEX = "^\\[([a-zA-Z_][a-zA-Z0-9_]*)\\]$";
    private static final String KEYVALUE_REGEX = "^([a-zA-Z_][a-zA-Z0-9_]*)=(.*)$";
    
    public ParseResult<Configuration> parse(String input) {
        String[] lines = input.split("\n", -1);
        Configuration config = new Configuration();
        ParseState state = new ParseState();
        
        for (int i = 0; i < lines.length; i++) {
            state.processLine(config, lines[i], i + 1);
        }
        
        return state.errors.isEmpty() ? 
            ParseResult.success(config) : 
            ParseResult.failure(state.errors);
    }
    
    private class ParseState {
        String currentSection = null;
        List<ValidationError> errors = new ArrayList<>();
        
        void processLine(Configuration config, String line, int lineNumber) {
            if (isBlankOrComment(line)) return;
            if (isSectionLine(line)) {
                currentSection = extractSectionName(line);
                return;
            }
            if (isKeyValueLine(line)) {
                ValidationError error = parseKeyValue(config, currentSection, line, lineNumber);
                if (error != null) errors.add(error);
                return;
            }
            errors.add(new ValidationError(lineNumber, "Malformed line"));
        }
    }
    
    private boolean isBlankOrComment(String line) {
        String trimmed = line.trim();
        return trimmed.isEmpty() || trimmed.startsWith("#");
    }
    
    private boolean isSectionLine(String line) {
        return line.trim().matches(SECTION_REGEX);
    }
    
    private String extractSectionName(String line) {
        String trimmed = line.trim();
        return trimmed.substring(1, trimmed.length() - 1);
    }
    
    private boolean isKeyValueLine(String line) {
        return line.trim().matches(KEYVALUE_REGEX);
    }
    
    private ValidationError parseKeyValue(
            Configuration config,
            String section,
            String line,
            int lineNumber) {
        
        if (section == null) {
            return new ValidationError(lineNumber, "Key-value pair outside section");
        }
        
        String trimmed = line.trim();
        int eqIndex = trimmed.indexOf('=');
        String key = trimmed.substring(0, eqIndex);
        String value = trimmed.substring(eqIndex + 1);
        
        try {
            applyValue(config, section, key, value);
        } catch (IllegalArgumentException e) {
            return new ValidationError(lineNumber, e.getMessage());
        }
        return null;
    }
    
    private void applyValue(Configuration config, String section, String key, String value) {
        switch (section) {
            case "server" -> applyServerValue(config.getServer(), key, value);
            case "database" -> applyDatabaseValue(config.getDatabase(), key, value);
            default -> throw new IllegalArgumentException("Unknown section: " + section);
        }
    }
    
    private void applyServerValue(ServerConfig server, String key, String value) {
        switch (key) {
            case "port" -> server.setPort(parseInt(value));
            case "timeout" -> server.setTimeout(Duration.parse(value));
            case "debug" -> server.setDebug(parseBoolean(value));
            default -> throw new IllegalArgumentException("Unknown key 'server." + key + "'");
        }
    }
    
    private void applyDatabaseValue(DatabaseConfig database, String key, String value) {
        switch (key) {
            case "max_connections" -> database.setMaxConnections(parseInt(value));
            case "connection_timeout" -> database.setConnectionTimeout(Duration.parse(value));
            default -> throw new IllegalArgumentException("Unknown key 'database." + key + "'");
        }
    }
    
    private int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer: " + value);
        }
    }
    
    private boolean parseBoolean(String value) {
        String trimmed = value.trim().toLowerCase();
        return switch (trimmed) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException("Invalid boolean: " + value);
        };
    }
}
