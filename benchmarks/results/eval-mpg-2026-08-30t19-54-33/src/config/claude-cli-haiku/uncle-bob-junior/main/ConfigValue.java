public sealed interface ConfigValue {
    record IntValue(int value) implements ConfigValue {}
    record BoolValue(boolean value) implements ConfigValue {}
    record DurationValue(Duration value) implements ConfigValue {}
}
