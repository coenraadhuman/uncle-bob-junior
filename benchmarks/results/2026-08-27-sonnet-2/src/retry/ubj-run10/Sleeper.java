// Sleeper.java
import java.time.Duration;

/**
 * Seam for waiting between retries. Production code uses {@link #threadSleep()};
 * tests can supply a fake that records or fast-forwards the wait.
 */
@FunctionalInterface
public interface Sleeper {

    void sleep(Duration duration) throws InterruptedException;

    static Sleeper threadSleep() {
        return duration -> Thread.sleep(duration.toMillis());
    }
}
