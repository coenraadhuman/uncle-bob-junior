public class ParseResult<T> {
    private final T value;
    private final List<ValidationError> errors;
    
    private ParseResult(T value, List<ValidationError> errors) {
        this.value = value;
        this.errors = errors;
    }
    
    public static <T> ParseResult<T> success(T value) {
        return new ParseResult<>(value, List.of());
    }
    
    public static <T> ParseResult<T> failure(List<ValidationError> errors) {
        return new ParseResult<>(null, errors);
    }
    
    public boolean isSuccess() { return errors.isEmpty(); }
    public T getValue() { return value; }
    public List<ValidationError> getErrors() { return errors; }
}
