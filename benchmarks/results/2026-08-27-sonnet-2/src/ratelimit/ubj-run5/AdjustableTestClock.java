// AdjustableTestClock.java
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

final class AdjustableTestClock extends Clock {
    private final ZoneId zone;
    private Instant currentInstant;

    AdjustableTestClock(Instant start) {
        this(start, ZoneOffset.UTC);
    }

    private AdjustableTestClock(Instant start, ZoneId zone) {
        this.currentInstant = start;
        this.zone = zone;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new AdjustableTestClock(currentInstant, zone);
    }

    @Override
    public Instant instant() {
        return currentInstant;
    }

    void advanceBy(Duration duration) {
        currentInstant = currentInstant.plus(duration);
    }
}
