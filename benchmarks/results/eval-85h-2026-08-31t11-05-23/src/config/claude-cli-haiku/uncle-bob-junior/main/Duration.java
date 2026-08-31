public final class Duration {
    private final long seconds;
    
    private Duration(long seconds) {
        if (seconds < 0) throw new IllegalArgumentException("Duration cannot be negative");
        this.seconds = seconds;
    }
    
    public static Duration ofSeconds(long seconds) {
        return new Duration(seconds);
    }
    
    public static Duration ofMinutes(long minutes) {
        return new Duration(minutes * 60);
    }
    
    public static Duration parse(String value) {
        String trimmed = value.trim();
        
        if (trimmed.endsWith("s")) {
            return ofSeconds(parseDurationNumber(trimmed, "s"));
        }
        if (trimmed.endsWith("m")) {
            return ofMinutes(parseDurationNumber(trimmed, "m"));
        }
        throw new IllegalArgumentException("Duration must end with 's' or 'm': " + value);
    }
    
    private static long parseDurationNumber(String trimmed, String suffix) {
        try {
            return Long.parseLong(trimmed.substring(0, trimmed.length() - 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid duration: " + trimmed);
        }
    }
    
    public long toSeconds() {
        return seconds;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Duration)) return false;
        return seconds == ((Duration) o).seconds;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(seconds);
    }
}
