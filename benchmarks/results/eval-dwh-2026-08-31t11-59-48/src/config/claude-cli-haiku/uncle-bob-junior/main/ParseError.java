public record ParseError(int line, String message) {
    @Override public String toString() {
        return line == 0 ? message : "Line " + line + ": " + message;
    }
}
