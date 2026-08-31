class BooleanKey extends ConfigKey {
    BooleanKey(String name, boolean defaultValue) {
        super(name, defaultValue);
    }
    
    @Override
    Object parse(String text) {
        if ("true".equalsIgnoreCase(text) || "yes".equalsIgnoreCase(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text) || "no".equalsIgnoreCase(text)) {
            return false;
        }
        throw new IllegalArgumentException("Expected boolean (true/false/yes/no), got: " + text);
    }
}
