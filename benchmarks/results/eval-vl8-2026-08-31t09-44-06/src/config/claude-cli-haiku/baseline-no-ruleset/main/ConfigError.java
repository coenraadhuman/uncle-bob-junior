class ConfigError {
    final int lineNumber;
    final String message;
    
    ConfigError(int lineNumber, String message) {
        this.lineNumber = lineNumber;
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "Line " + lineNumber + ": " + message;
    }
}
