package com.plg.retry;

import java.time.Duration;

@FunctionalInterface
public interface Sleeper {

    Sleeper SYSTEM = duration -> Thread.sleep(duration.toMillis());

    void sleep(Duration duration) throws InterruptedException;
}
