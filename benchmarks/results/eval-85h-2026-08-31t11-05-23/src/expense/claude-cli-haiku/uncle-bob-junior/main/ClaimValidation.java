import java.util.*;

public record ClaimValidation(boolean valid, List<String> errors) {}
