public class ValidationError {
    public final int lineNumber;
    public final String message;
    
    public ValidationError(int lineNumber, String message) {
        this.lineNumber = lineNumber;
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "Line " + lineNumber + ": " + message;
    }
}
