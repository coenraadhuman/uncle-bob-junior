import java.util.*;
import java.util.regex.*;

public class ConfigParser {
    
    public static class ValidationException extends Exception {
        public final int lineNumber;
        
        public ValidationException(String message, int lineNumber) {
            super("Line " + lineNumber + ": " + message);
            this.lineNumber = lineNumber;
        }
    }
    
    public static class Config {
        public int port = 8080;
        public int maxConnections = 10;
        public boolean debug = false;
        public long timeout = 30000;
        
        @Override
        public String toString() {
            return "Config{" +
                    "port=" + port +
                    ", maxConnections=" + maxConnections +
                    ", debug=" + debug +
                    ", timeout=" + timeout +
                    '}';
        }
    }
    
    public static class Parser {
        private static final Set<String> KNOWN_KEYS = Set.of(
            "port", "maxConnections", "debug", "timeout"
        );
        
        private static final Map<String, String> KEY_TYPES = Map.of(
            "port", "int",
            "maxConnections", "int",
            "debug", "boolean",
            "timeout", "duration"
        );
        
        public Config parse(String content) throws ValidationException {
            Config config = new Config();
            String[] lines = content.split("\n");
            
            for (int i = 0; i < lines.length; i++) {
                int lineNumber = i + 1;
                String line = lines[i].trim();
                
                int commentIndex = line.indexOf('#');
                if (commentIndex != -1) {
                    line = line.substring(0, commentIndex).trim();
                }
                
                if (line.isEmpty()) {
                    continue;
                }
                
                if (line.startsWith("[") && line.endsWith("]")) {
                    continue;
                }
                
                if (!line.contains("=")) {
                    throw new ValidationException("Malformed line: missing '='", lineNumber);
                }
                
                String[] parts = line.split("=", 2);
                if (parts.length != 2) {
                    throw new ValidationException("Malformed line: invalid format", lineNumber);
                }
                
                String key = parts[0].trim();
                String value = parts[1].trim();
                
                if (key.isEmpty() || value.isEmpty()) {
                    throw new ValidationException("Malformed line: empty key or value", lineNumber);
                }
                
                if (!KNOWN_KEYS.contains(key)) {
                    throw new ValidationException("Unknown key: " + key, lineNumber);
                }
                
                String type = KEY_TYPES.get(key);
                
                try {
                    switch (key) {
                        case "port" -> config.port = parseInteger(value);
                        case "maxConnections" -> config.maxConnections = parseInteger(value);
                        case "debug" -> config.debug = parseBoolean(value);
                        case "timeout" -> config.timeout = parseDuration(value);
                    }
                } catch (NumberFormatException e) {
                    throw new ValidationException("Invalid " + type + " value: " + value, lineNumber);
                }
            }
            
            return config;
        }
        
        private int parseInteger(String value) throws NumberFormatException {
            return Integer.parseInt(value);
        }
        
        private boolean parseBoolean(String value) throws NumberFormatException {
            if ("true".equalsIgnoreCase(value)) {
                return true;
            } else if ("false".equalsIgnoreCase(value)) {
                return false;
            }
            throw new NumberFormatException("Invalid boolean: " + value);
        }
        
        private long parseDuration(String value) throws NumberFormatException {
            Pattern pattern = Pattern.compile("^(\\d+)([smhd])$");
            Matcher matcher = pattern.matcher(value);
            
            if (!matcher.matches()) {
                throw new NumberFormatException("Invalid duration format: " + value);
            }
            
            long amount = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            
            return switch (unit) {
                case "s" -> amount * 1000;
                case "m" -> amount * 60 * 1000;
                case "h" -> amount * 60 * 60 * 1000;
                case "d" -> amount * 24 * 60 * 60 * 1000;
                default -> throw new NumberFormatException("Unknown time unit: " + unit);
            };
        }
    }
    
    public static void main(String[] args) throws ValidationException {
        String validConfig = """
            # Server configuration
            [server]
            port=8080
            maxConnections=100
            timeout=5m
            debug=true
            """;
        
        Parser parser = new Parser();
        Config config = parser.parse(validConfig);
        System.out.println("Parsed: " + config);
        
        try {
            parser.parse("unknownKey=123");
        } catch (ValidationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
