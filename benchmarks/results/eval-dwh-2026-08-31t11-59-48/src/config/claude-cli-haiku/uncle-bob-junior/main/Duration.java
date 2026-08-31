public class Duration {
    private final long milliseconds;
    
    public static Duration parse(String raw) {
        if (!raw.matches("\\d+[smh]")) {
            throw new IllegalArgumentException("Invalid duration: " + raw);
        }
        
        long amount = Long.parseLong(raw.replaceAll("[^0-9]", ""));
        char unit = raw.charAt(raw.length() - 1);
        long millis = amount * unitToMillis(unit);
        
        return new Duration(millis);
    }
    
    private static long unitToMillis(char unit) {
        return switch (unit) {
            case 's' -> 1000;
            case 'm' -> 60_000;
            case 'h' -> 3_600_000;
            default -> throw new IllegalArgumentException("Unknown unit: " + unit);
        };
    }
    
    private Duration(long milliseconds) {
        this.milliseconds = milliseconds;
    }
    
    public long milliseconds() { return milliseconds; }
    public long seconds() { return milliseconds / 1000; }
}
