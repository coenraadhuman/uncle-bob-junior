class ConfigurationException extends Exception {
    private final int lineNumber;
    
    public ConfigurationException(int lineNumber, String message) {
        super("Line " + lineNumber + ": " + message);
        this.lineNumber = lineNumber;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
}
