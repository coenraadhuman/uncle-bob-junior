import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Duration {
    private final long seconds;
    
    private Duration(long seconds) {
        this.seconds = seconds;
    }
    
    public static Duration parse(String value) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("^(\\d+)([smh])$");
        Matcher matcher = pattern.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid format (expected e.g. 30s, 5m, 1h)");
        }
        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);
        long totalSeconds = switch(unit) {
            case "s" -> amount;
            case "m" -> amount * 60;
            case "h" -> amount * 3600;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
        return new Duration(totalSeconds);
    }
    
    public static Duration ofSeconds(long seconds) {
        return new Duration(seconds);
    }
    
    public long getSeconds() {
        return seconds;
    }
    
    @Override
    public String toString() {
        if (seconds % 3600 == 0) return (seconds / 3600) + "h";
        if (seconds % 60 == 0) return (seconds / 60) + "m";
        return seconds + "s";
    }
}
