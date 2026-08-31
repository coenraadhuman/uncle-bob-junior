public class ConfigException extends Exception {
    public ConfigException(String message) {
        super(message);
    }
    
    public ConfigException(String message, int lineNumber) {
        super("Line " + lineNumber + ": " + message);
    }
}
