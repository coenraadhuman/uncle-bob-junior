package com.example.retry;

import java.time.Duration;

/** Seam around the blocking wait so tests can avoid real time delays. */
@FunctionalInterface
public interface Sleeper {

    void sleep(Duration delay) throws InterruptedException;
}
