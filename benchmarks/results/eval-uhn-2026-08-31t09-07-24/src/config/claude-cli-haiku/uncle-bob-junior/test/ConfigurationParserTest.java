import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import java.util.Map;

public class ConfigurationParserTest {
    
    @Test
    void parsesIntegers() throws ConfigurationParser.ConfigurationException {
        ConfigurationParser.Configuration config = ConfigurationParser.parse(
            "timeout=30",
            Map.of("timeout", 0)
        );
        assertThat(config.getInt("timeout")).isEqualTo(30);
    }
    
    @Test
    void parsesBooleanVariants() throws ConfigurationParser.ConfigurationException {
        ConfigurationParser.Configuration config = ConfigurationParser.parse(
            "a=true\nb=yes\nc=on\nd=false\ne=no\nf=off",
            Map.of("a", false, "b", false, "c", false, "d", false, "e", false, "f", false)
        );
        assertThat(config.getBoolean("a")).isTrue();
        assertThat(config.getBoolean("b")).isTrue();
        assertThat(config.getBoolean("c")).isTrue();
        assertThat(config.getBoolean("d")).isFalse();
        assertThat(config.getBoolean("e")).isFalse();
        assertThat(config.getBoolean("f")).isFalse();
    }
    
    @Test
    void parsesDurations() throws ConfigurationParser.ConfigurationException {
        ConfigurationParser.Configuration config = ConfigurationParser.parse(
            "sec=30s\nmin=5m\nhour=2h\nday=1d",
            Map.of("sec", 0L, "min", 0L, "hour", 0L, "day", 0L)
        );
        assertThat(config.getDuration("sec")).isEqualTo(30_000);
        assertThat(config.getDuration("min")).isEqualTo(300_000);
        assertThat(config.getDuration("hour")).isEqualTo(7_200_000);
        assertThat(config.getDuration("day")).isEqualTo(86_400_000);
    }
    
    @Test
    void ignoresCommentsAndBlankLines() throws ConfigurationParser.ConfigurationException {
        ConfigurationParser.Configuration config = ConfigurationParser.parse(
            "# comment\n\nretries=3\n# another",
            Map.of("retries", 0)
        );
        assertThat(config.getInt("retries")).isEqualTo(3);
    }
    
    @Test
    void appliesDefaults() throws ConfigurationParser.ConfigurationException {
        ConfigurationParser.Configuration config = ConfigurationParser.parse(
            "",
            Map.of("timeout", 60, "debug", false)
        );
        assertThat(config.getInt("timeout")).isEqualTo(60);
        assertThat(config.getBoolean("debug")).isFalse();
    }
    
    @Test
    void overridesDefaults() throws ConfigurationParser.ConfigurationException {
        ConfigurationParser.Configuration config = ConfigurationParser.parse(
            "timeout=30",
            Map.of("timeout", 60)
        );
        assertThat(config.getInt("timeout")).isEqualTo(30);
    }
    
    @Test
    void reportsMalformedLine() {
        ConfigurationParser.ConfigurationException ex = assertThrows(
            ConfigurationParser.ConfigurationException.class,
            () -> ConfigurationParser.parse("no equals", Map.of("key", 0))
        );
        assertThat(ex.errors.get(0)).contains("Line 1").contains("malformed");
    }
    
    @Test
    void reportsUnknownKey() {
        ConfigurationParser.ConfigurationException ex = assertThrows(
            ConfigurationParser.ConfigurationException.class,
            () -> ConfigurationParser.parse("unknown=1", Map.of())
        );
        assertThat(ex.errors.get(0)).contains("Line 1").contains("unknown key");
    }
    
    @Test
    void reportsInvalidValue() {
        ConfigurationParser.ConfigurationException ex = assertThrows(
            ConfigurationParser.ConfigurationException.class,
            () -> ConfigurationParser.parse("timeout=notanumber", Map.of("timeout", 0))
        );
        assertThat(ex.errors.get(0)).contains("Line 1").contains("invalid value");
    }
    
    @Test
    void collectsAllErrors() {
        ConfigurationParser.ConfigurationException ex = assertThrows(
            ConfigurationParser.ConfigurationException.class,
            () -> ConfigurationParser.parse(
                "bad_line\nunknown=1\ntimeout=bad",
                Map.of("timeout", 0)
            )
        );
        assertThat(ex.errors).hasSize(3);
        assertThat(ex.errors.get(0)).contains("Line 1");
        assertThat(ex.errors.get(1)).contains("Line 2");
        assertThat(ex.errors.get(2)).contains("Line 3");
    }
    
    @Test
    void ignoresSectionHeaders() throws ConfigurationParser.ConfigurationException {
        ConfigurationParser.Configuration config = ConfigurationParser.parse(
            "[database]\nport=5432",
            Map.of("port", 0)
        );
        assertThat(config.getInt("port")).isEqualTo(5432);
    }
    
    @Test
    void handlesWhitespace() throws ConfigurationParser.ConfigurationException {
        ConfigurationParser.Configuration config = ConfigurationParser.parse(
            "  timeout  =  30  ",
            Map.of("timeout", 0)
        );
        assertThat(config.getInt("timeout")).isEqualTo(30);
    }
}
