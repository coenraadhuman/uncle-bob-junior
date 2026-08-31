class Value {
    static class IntValue {
        final int value;
        IntValue(int value) { this.value = value; }
    }
    
    static class BoolValue {
        final boolean value;
        BoolValue(boolean value) { this.value = value; }
    }
    
    static class DurationValue {
        final long millis;
        DurationValue(long millis) { this.millis = millis; }
    }
}
