public class Configuration {
    private static final int DEFAULT_TIMEOUT = 30;
    private static final boolean DEFAULT_DEBUG = false;
    private static final long DEFAULT_RETRY_DELAY_MS = 5000;
    
    private final int timeout;
    private final boolean debug;
    private final long retryDelayMs;
    private final List<ValidationError> errors;
    
    Configuration(Map<String, ConfigValue> values, List<ValidationError> errors) {
        this.timeout = extractInt(values, "timeout", DEFAULT_TIMEOUT);
        this.debug = extractBool(values, "debug", DEFAULT_DEBUG);
        this.retryDelayMs = extractDuration(values, "retry_delay", DEFAULT_RETRY_DELAY_MS);
        this.errors = errors;
    }
    
    private static int extractInt(Map<String, ConfigValue> values, String key, int defaultValue) {
        return values.get(key) instanceof ConfigValue.IntValue iv ? iv.value() : defaultValue;
    }
    
    private static boolean extractBool(Map<String, ConfigValue> values, String key, boolean defaultValue) {
        return values.get(key) instanceof ConfigValue.BoolValue bv ? bv.value() : defaultValue;
    }
    
    private static long extractDuration(Map<String, ConfigValue> values, String key, long defaultValue) {
        return values.get(key) instanceof ConfigValue.DurationValue dv ? dv.millis() : defaultValue;
    }
    
    public int getTimeout() { return timeout; }
    public boolean isDebug() { return debug; }
    public long getRetryDelayMs() { return retryDelayMs; }
    public List<ValidationError> getErrors() { return errors; }
    public boolean hasErrors() { return !errors.isEmpty(); }
    
    public static Configuration parse(String input) {
        return new ConfigParser().parse(input);
    }
}
