class ConfigParser {
    private static final java.util.regex.Pattern SECTION = 
        java.util.regex.Pattern.compile("^\\s*\\[([^\\]]+)\\]\\s*$");
    private static final java.util.regex.Pattern KEY_VALUE = 
        java.util.regex.Pattern.compile("^\\s*([a-z_][a-z0-9_]*)\\s*=\\s*(.+?)\\s*$", 
        java.util.regex.Pattern.CASE_INSENSITIVE);
    private static final java.util.regex.Pattern COMMENT_OR_BLANK = 
        java.util.regex.Pattern.compile("^\\s*(?:#.*)?$");
    private static final Set<String> KNOWN_KEYS = Set.of("timeout", "debug", "retry_delay");
    
    Configuration parse(String input) {
        Map<String, ConfigValue> values = new HashMap<>();
        List<ValidationError> errors = new ArrayList<>();
        String[] lines = input.split("\n", -1);
        
        for (int i = 0; i < lines.length; i++) {
            parseLine(lines[i], i + 1, values, errors);
        }
        
        return new Configuration(values, errors);
    }
    
    private void parseLine(String line, int lineNumber, Map<String, ConfigValue> values,
            List<ValidationError> errors) {
        if (COMMENT_OR_BLANK.matcher(line).matches()) {
            return;
        }
        
        if (SECTION.matcher(line).matches()) {
            return;
        }
        
        java.util.regex.Matcher kv = KEY_VALUE.matcher(line);
        if (!kv.matches()) {
            errors.add(new ValidationError(lineNumber, "Malformed line"));
            return;
        }
        
        String key = kv.group(1);
        if (!KNOWN_KEYS.contains(key)) {
            errors.add(new ValidationError(lineNumber, "Unknown key: " + key));
            return;
        }
        
        parseKeyValue(key, kv.group(2), lineNumber, values, errors);
    }
    
    private void parseKeyValue(String key, String valueStr, int lineNumber,
            Map<String, ConfigValue> values, List<ValidationError> errors) {
        ConfigValue value = switch (key) {
            case "timeout" -> parseInteger(valueStr, lineNumber, errors);
            case "debug" -> parseBoolean(valueStr, lineNumber, errors);
            case "retry_delay" -> parseDuration(valueStr, lineNumber, errors);
            default -> null;
        };
        
        if (value != null) {
            values.put(key, value);
        }
    }
    
    private ConfigValue parseInteger(String str, int lineNumber, List<ValidationError> errors) {
        try {
            return new ConfigValue.IntValue(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            errors.add(new ValidationError(lineNumber, "Invalid integer: " + str));
            return null;
        }
    }
    
    private ConfigValue parseBoolean(String str, int lineNumber, List<ValidationError> errors) {
        String lower = str.toLowerCase();
        
        if (lower.equals("true") || lower.equals("yes") || lower.equals("on") || lower.equals("1")) {
            return new ConfigValue.BoolValue(true);
        }
        
        if (lower.equals("false") || lower.equals("no") || lower.equals("off") || lower.equals("0")) {
            return new ConfigValue.BoolValue(false);
        }
        
        errors.add(new ValidationError(lineNumber, "Invalid boolean: " + str));
        return null;
    }
    
    private ConfigValue parseDuration(String str, int lineNumber, List<ValidationError> errors) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("^(\\d+)\\s*([a-z]+)$",
            java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m = p.matcher(str);
        
        if (!m.matches()) {
            errors.add(new ValidationError(lineNumber, "Invalid duration: " + str));
            return null;
        }
        
        long value = Long.parseLong(m.group(1));
        long multiplier = durationMultiplier(m.group(2).toLowerCase());
        
        if (multiplier < 0) {
            errors.add(new ValidationError(lineNumber, "Unknown duration unit: " + m.group(2)));
            return null;
        }
        
        return new ConfigValue.DurationValue(value * multiplier);
    }
    
    private long durationMultiplier(String unit) {
        return switch (unit) {
            case "ms" -> 1;
            case "s" -> 1_000;
            case "m" -> 60_000;
            case "h" -> 3_600_000;
            default -> -1;
        };
    }
}
