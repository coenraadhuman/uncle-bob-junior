record ValidationError(int lineNumber, String message) {
    @Override
    public String toString() {
        return "Line " + lineNumber + ": " + message;
    }
}
