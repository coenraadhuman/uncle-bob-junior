package com.plg.retry;

import java.time.Duration;

/** Boundary for waiting, so tests can avoid real delays. */
public interface Sleeper {

    void sleep(Duration duration) throws InterruptedException;

    static Sleeper realTime() {
        return duration -> Thread.sleep(duration.toMillis());
    }
}
