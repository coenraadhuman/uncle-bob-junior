class IntKey extends ConfigKey {
    IntKey(String name, int defaultValue) {
        super(name, defaultValue);
    }
    
    @Override
    Object parse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Expected integer, got: " + text);
        }
    }
}
