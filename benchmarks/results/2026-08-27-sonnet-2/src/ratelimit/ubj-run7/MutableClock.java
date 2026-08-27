package com.plg.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

final class MutableClock extends Clock {

    private Instant currentInstant;

    private MutableClock(Instant startingInstant) {
        this.currentInstant = startingInstant;
    }

    static MutableClock startingAt(Instant startingInstant) {
        return new MutableClock(startingInstant);
    }

    void advanceBy(Duration duration) {
        currentInstant = currentInstant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new UnsupportedOperationException("not needed for testing");
    }

    @Override
    public Instant instant() {
        return currentInstant;
    }
}
