import java.util.regex.*;

public class Duration {
    private final long millis;
    
    private Duration(long millis) {
        this.millis = millis;
    }
    
    public static Duration parse(String value) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("^(\\d+)([smhd])$");
        Matcher matcher = pattern.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid duration format: " + value);
        }
        
        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);
        long millis = switch (unit) {
            case "s" -> amount * 1000;
            case "m" -> amount * 60 * 1000;
            case "h" -> amount * 60 * 60 * 1000;
            case "d" -> amount * 24 * 60 * 60 * 1000;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
        return new Duration(millis);
    }
    
    public long toMillis() { return millis; }
    public long toSeconds() { return millis / 1000; }
    
    @Override
    public String toString() { return millis + "ms"; }
}
