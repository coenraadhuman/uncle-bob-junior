import java.util.*;
import java.util.regex.*;

public record ConfigKey(String name, ConfigType type, Object defaultValue) {}
