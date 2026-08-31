import java.util.*;
import java.util.regex.*;

class ConfigDuration {
    private final long millis;
    
    private ConfigDuration(long millis) {
        this.millis = millis;
    }
    
    public static ConfigDuration parse(String value) {
        Pattern pattern = Pattern.compile("^(\\d+)([smhd])$");
        Matcher matcher = pattern.matcher(value.toLowerCase().trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid duration: " + value);
        }
        
        long amount = Long.parseLong(matcher.group(1));
        char unit = matcher.group(2).charAt(0);
        
        long millis = switch (unit) {
            case 's' -> amount * 1000;
            case 'm' -> amount * 60 * 1000;
            case 'h' -> amount * 60 * 60 * 1000;
            case 'd' -> amount * 24 * 60 * 60 * 1000;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
        
        return new ConfigDuration(millis);
    }
    
    public long getMillis() {
        return millis;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigDuration duration = (ConfigDuration) o;
        return millis == duration.millis;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(millis);
    }
    
    @Override
    public String toString() {
        return millis + "ms";
    }
}
