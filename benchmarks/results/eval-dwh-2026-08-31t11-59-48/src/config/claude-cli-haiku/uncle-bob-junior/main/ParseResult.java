public record ParseResult<T>(T value, List<ParseError> errors, boolean ok) {
    public static <T> ParseResult<T> ok(T value) {
        return new ParseResult<>(value, List.of(), true);
    }
    
    public static <T> ParseResult<T> error(List<ParseError> errors) {
        return new ParseResult<>(null, errors, false);
    }
}
