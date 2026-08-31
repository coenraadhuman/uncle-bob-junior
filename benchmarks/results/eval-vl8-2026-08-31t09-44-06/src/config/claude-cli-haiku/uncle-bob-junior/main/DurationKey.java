import java.time.Duration;

class DurationKey extends ConfigKey {
    DurationKey(String name, Duration defaultValue) {
        super(name, defaultValue);
    }
    
    @Override
    Object parse(String text) {
        if (text.endsWith("s")) {
            try {
                long seconds = Long.parseLong(text.substring(0, text.length() - 1));
                return Duration.ofSeconds(seconds);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid duration seconds: " + text);
            }
        }
        if (text.endsWith("m")) {
            try {
                long minutes = Long.parseLong(text.substring(0, text.length() - 1));
                return Duration.ofMinutes(minutes);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid duration minutes: " + text);
            }
        }
        throw new IllegalArgumentException("Expected duration format (e.g., 30s or 5m), got: " + text);
    }
}
