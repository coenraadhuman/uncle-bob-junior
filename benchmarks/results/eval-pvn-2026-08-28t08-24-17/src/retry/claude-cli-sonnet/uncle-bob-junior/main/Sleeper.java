package retry;

import java.time.Duration;

@FunctionalInterface
interface Sleeper {
    void sleep(Duration delay) throws InterruptedException;
}
