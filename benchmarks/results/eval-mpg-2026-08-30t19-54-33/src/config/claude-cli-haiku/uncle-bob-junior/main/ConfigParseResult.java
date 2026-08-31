import java.util.*;

record ConfigParseResult(Config config, List<ConfigError> errors) {
  public boolean isValid() {
    return errors.isEmpty();
  }
}
