sealed interface ConfigValue {
    record IntValue(int value) implements ConfigValue {}
    record BoolValue(boolean value) implements ConfigValue {}
    record DurationValue(long millis) implements ConfigValue {}
}
