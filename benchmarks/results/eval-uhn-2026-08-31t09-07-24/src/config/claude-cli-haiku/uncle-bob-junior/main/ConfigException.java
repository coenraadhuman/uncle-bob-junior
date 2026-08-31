public static class ConfigException extends Exception {
    private final int lineNumber;
    
    public ConfigException(String message, int lineNumber) {
        super("Line " + lineNumber + ": " + message);
        this.lineNumber = lineNumber;
    }
    
    public int lineNumber() { return lineNumber; }
}
