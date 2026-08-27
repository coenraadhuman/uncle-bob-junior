package com.example.retry;

import java.time.Duration;

/**
 * Waits for a fixed duration between retry attempts.
 * Implementations may throw {@link InterruptedException} if the wait is interrupted.
 */
@FunctionalInterface
interface Sleeper {
    void sleep(Duration duration) throws InterruptedException;
}
